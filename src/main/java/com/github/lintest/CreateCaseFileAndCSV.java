package com.github.lintest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;


public class CreateCaseFileAndCSV extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取到editor和project
        Project project = e.getData(CommonDataKeys.PROJECT);
        Navigatable navigatable = e.getData(CommonDataKeys.NAVIGATABLE);
        String selectedPackagePath = navigatable.toString();
        String fullPath = "";

        if (selectedPackagePath.startsWith("PsiDirectory:")) {
            selectedPackagePath = selectedPackagePath.split(project.getName())[1];

            if (System.getProperty("os.name").startsWith("Windows")) {
                selectedPackagePath = selectedPackagePath.replaceAll("\\\\", ".");
            } else {
                selectedPackagePath = selectedPackagePath.replaceAll(File.separator, ".");
            }

            if (selectedPackagePath.startsWith(".")) {
                selectedPackagePath = selectedPackagePath.replaceFirst(".", "");
            }

            fullPath = e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath();

        } else {
            String fileName = selectedPackagePath.split(":")[1];
            fullPath = e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath().substring(0, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath().length() - fileName.length());
            selectedPackagePath = e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath().split(project.getName())[1];
            selectedPackagePath = selectedPackagePath.substring(0, selectedPackagePath.length() - fileName.length());

            if (System.getProperty("os.name").startsWith("Windows")) {
                selectedPackagePath = selectedPackagePath.replaceAll("\\\\", ".");
            } else {
                selectedPackagePath = selectedPackagePath.replaceAll(File.separator, ".");
            }

            if (selectedPackagePath.startsWith(".")) {
                selectedPackagePath = selectedPackagePath.replaceFirst(".", "");
            }

            if (selectedPackagePath.endsWith(".")) {
                selectedPackagePath = selectedPackagePath.substring(0, selectedPackagePath.length() - 1);
            }
        }

        if ((!selectedPackagePath.startsWith("tests.") && !"tests".equals(selectedPackagePath))) {
            Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                    "不合法的包路径:", Messages.getErrorIcon());
            return;
        }

        CreateCaseFileAndCsvUI createCaseFileAndCsvUI = new CreateCaseFileAndCsvUI();
        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(createCaseFileAndCsvUI.getRootPanel());
        dialogBuilder.setTitle("Add a testcase file to the package");

        if (System.getProperty("os.name").startsWith("Windows")) {
            createCaseFileAndCsvUI.setPackagePath(selectedPackagePath.replaceAll("\\\\", "."));
        } else {
            createCaseFileAndCsvUI.setPackagePath(selectedPackagePath.replaceAll(File.separator, "."));
        }
        createCaseFileAndCsvUI.getPackagePath().setEditable(false);
        String finalFullPath = fullPath;
        dialogBuilder.setOkOperation(() -> {
            dialogBuilder.getDialogWrapper().close(0);
            String fileName = createCaseFileAndCsvUI.getFileName();
            fileName = fileName.trim();

            if (fileName.contains(".")) {
                if (!fileName.endsWith(".py")) {
                    Messages.showMessageDialog("1. 文件名只能是 字母,数字和下划线 的组合(其中首字符只能是 字母) 2.只支持 .py后缀 或者 无后缀", "无效的文件名", Messages.getErrorIcon());
                    return;
                }

                fileName = fileName.substring(0, fileName.length() - 3);
            }

            String pyFileRegx = "^[a-zA-Z]+[_a-zA-Z0-9]*$";
            Pattern pattern = Pattern.compile(pyFileRegx);

            if (!pattern.matcher(fileName).find()) {
                Messages.showMessageDialog("1. 文件名只能是 字母,数字和下划线 的组合(其中首字符只能是 字母) 2.只支持 .py后缀 或者 无后缀", "无效的文件名", Messages.getErrorIcon());
                return;
            }

//            fileName += ".py";

            File f;
            if (System.getProperty("os.name").startsWith("Windows")) {
                f = new File(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + "\\" + fileName + ".py");
            } else {
                f = new File(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + File.separator + fileName + ".py");
            }

            if (f.exists()) {
                Messages.showMessageDialog("文件: " + fileName + ".py" + " 已存在", "错误", Messages.getErrorIcon());
                return;
            }

            File csv_f;
//            System.out.println(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + File.separator + fileName.substring(0, fileName.length() - 3) + ".csv");

            if (System.getProperty("os.name").startsWith("Windows")) {
                csv_f = new File(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + "\\" + fileName + ".csv");
            } else {
                csv_f = new File(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + File.separator + fileName + ".csv");
            }

            if (csv_f.exists()) {
                Messages.showMessageDialog("文件: " + fileName + ".csv" + " 已存在", "错误", Messages.getErrorIcon());
                return;
            }

            // 先生成 .py 文件
            try {
                String filePath = "";

                if (System.getProperty("os.name").startsWith("Windows")) {
                    filePath = finalFullPath + "\\" + fileName + ".py";
                } else {
                    filePath = finalFullPath + File.separator + fileName + ".py";
                }

                Path fP = Path.of(filePath);
                Files.createFile(fP);

                String codeDemoStr = "def login(self, username, password):\n" +
                        "    self.logger.info(\"login with username: %s, password: %s\" % (username, password))\n" +
                        "\n" +
                        "\n" +
                        "def run_test(self, username, password):\n" +
                        "    tag = \"test_login\"\n" +
                        "\n" +
                        "    self.logger.info(\"start run_test()...\")\n" +
                        "    login(self, username, password)";

                Files.writeString(fP, codeDemoStr + System.lineSeparator(), StandardOpenOption.APPEND);

                Messages.showMessageDialog(fileName + ".py", "成功创建", Messages.getInformationIcon());
                project.getBaseDir().refresh(false, true);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            // 再生成对应的 .csv 文件
            try {
                String csvFilePath = "";

                if (System.getProperty("os.name").startsWith("Windows")) {
                    csvFilePath = finalFullPath + "\\" + fileName + ".csv";
                } else {
                    csvFilePath = finalFullPath + File.separator + fileName + ".csv";
                }

                Path csvFP = Path.of(csvFilePath);
                Files.createFile(csvFP);

                String csvDemoStr = "id,username,password\n" +
                        "1,name1,password1\n" +
                        "2,name2,password2";

                Files.writeString(csvFP, csvDemoStr, StandardOpenOption.APPEND);

                Messages.showMessageDialog(fileName + ".csv", "成功创建", Messages.getInformationIcon());
                project.getBaseDir().refresh(false, true);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        dialogBuilder.show();
    }
}
