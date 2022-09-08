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


public class CreateATestCaseFile extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取到editor和project
        Project project = e.getData(CommonDataKeys.PROJECT);
        Navigatable navigatable = e.getData(CommonDataKeys.NAVIGATABLE);
        String selectedPackagePath = navigatable.toString();

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
        } else {
            Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                    "不合法的包路径:", Messages.getErrorIcon());
            return;
        }

        if (!selectedPackagePath.startsWith("tests.") && !"tests".equals(selectedPackagePath)) {
            Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                    "不合法的包路径:", Messages.getErrorIcon());
            return;
        }

        CreateTestCaseFileUI createTestCaseFileUI = new CreateTestCaseFileUI();
        createTestCaseFileUI.getApiCheckBox().setSelected(true);
        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(createTestCaseFileUI.getRootPanel());
        dialogBuilder.setTitle("Please enter the parameters");
        dialogBuilder.setOkOperation(() -> {
            dialogBuilder.getDialogWrapper().close(0);
            String fileName = createTestCaseFileUI.getFileName();
            fileName = fileName.trim().replaceAll(" +", "");
            if (fileName.startsWith(".")) {
                Messages.showMessageDialog("只支持 .py后缀 或者 无后缀", "无效的文件名", Messages.getErrorIcon());
                return;
            }
            if (fileName.contains(".")) {
                int count = 0;
                String[] fileNameCharList = new String[fileName.length()];
                for (int i = 0; i < fileName.length(); i++) {
                    fileNameCharList[i] = String.valueOf(fileName.toCharArray()[i]);
                }
                for (int i = 0; i < fileNameCharList.length; i++) {
                    System.out.println(fileNameCharList[i]);
                    if (".".equals(fileNameCharList[i])) {
                        count += 1;
                    }
                }

                if (count > 1) {
                    Messages.showMessageDialog("只支持 .py后缀 或者 无后缀", "无效的文件名", Messages.getErrorIcon());
                    return;
                } else if (count == 1) {
                    if (!fileName.endsWith(".py")) {
                        Messages.showMessageDialog("只支持 .py后缀 或者 无后缀", "无效的文件名", Messages.getErrorIcon());
                        return;
                    }
                }
            } else {
                fileName += ".py";
            }

            File f = new File(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + File.separator + fileName);
            if (f.exists()) {
                Messages.showMessageDialog("文件: " + fileName + " 已存在", "错误", Messages.getErrorIcon());
                return;
            }

            if (!createTestCaseFileUI.getUiCheckBox().isSelected() &&
                    !createTestCaseFileUI.getAndroidCheckBox().isSelected() &&
                    !createTestCaseFileUI.getIosCheckBox().isSelected()
            ) {
                // 如果 UI & Android & IOS 都没有选择， 则默认设置为 API type
                createTestCaseFileUI.getApiCheckBox().setSelected(true);
            }

            String superClsNamesStr = "";
            if (createTestCaseFileUI.getApiCheckBox().isSelected()) {
                superClsNamesStr += "APITestCase, ";
            }
            if (createTestCaseFileUI.getUiCheckBox().isSelected()) {
                superClsNamesStr += "UITestCase, ";
            }
            if (createTestCaseFileUI.getAndroidCheckBox().isSelected()) {
                superClsNamesStr += "AndroidTestCase, ";
            }
            if (createTestCaseFileUI.getIosCheckBox().isSelected()) {
                superClsNamesStr += "IOSTestCase, ";
            }

            try {
                String filePath = e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + File.separator + fileName;
                Path fP = Path.of(filePath);
                Files.createFile(fP);

                if (System.getProperty("os.name").startsWith("Windows")) {
                    if (createTestCaseFileUI.getApiCheckBox().isSelected()) {
                        Files.writeString(fP, "from lintest.api_testcase import APITestCase" + "\n", StandardOpenOption.APPEND);
                    }
                    if (createTestCaseFileUI.getUiCheckBox().isSelected()) {
                        Files.writeString(fP, "from lintest.ui_testcase import UITestCase" + "\n", StandardOpenOption.APPEND);
                    }
                    if (createTestCaseFileUI.getAndroidCheckBox().isSelected()) {
                        Files.writeString(fP, "from lintest.android_testcase import AndroidTestCase" + "\n", StandardOpenOption.APPEND);
                    }
                    if (createTestCaseFileUI.getIosCheckBox().isSelected()) {
                        Files.writeString(fP, "from lintest.ios_testcase import IOSTestCase" + "\n", StandardOpenOption.APPEND);
                    }

                    Files.writeString(fP, "\n\n", StandardOpenOption.APPEND);
                    superClsNamesStr = superClsNamesStr.substring(0, superClsNamesStr.length() - 2);

                    Files.writeString(fP, "class " + fileName.split(".")[0] + "(" + superClsNamesStr + "):\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "    tag = 'regression'" + "\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "    def setup(self):" + "\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "        self.logger.info(\"setup()...\")\n\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "    def teardown(self):" + "\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "        self.logger.info(\"teardown()...\")\n\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "    def run_test(self):" + "\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "        # 输入 self. 获得框架提供的基础能力\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "        self.logger.info(\"write the business code here\")\n", StandardOpenOption.APPEND);
                    Files.writeString(fP, "        self.requests.get(self, url=\"https://www.bing.com\")  # will auto log the request's params\n", StandardOpenOption.APPEND);

                    if (createTestCaseFileUI.getUiCheckBox().isSelected()) {
                        Files.writeString(fP, "        self.browser.get(\"https://www.bing.com\")\n", StandardOpenOption.APPEND);
                    }
                } else {
                    if (createTestCaseFileUI.getApiCheckBox().isSelected()) {
                        Files.writeString(fP, "from lintest.api_testcase import APITestCase" + System.lineSeparator(), StandardOpenOption.APPEND);
                    }
                    if (createTestCaseFileUI.getUiCheckBox().isSelected()) {
                        Files.writeString(fP, "from lintest.ui_testcase import UITestCase" + System.lineSeparator(), StandardOpenOption.APPEND);
                    }
                    if (createTestCaseFileUI.getAndroidCheckBox().isSelected()) {
                        Files.writeString(fP, "from lintest.android_testcase import AndroidTestCase" + System.lineSeparator(), StandardOpenOption.APPEND);
                    }
                    if (createTestCaseFileUI.getIosCheckBox().isSelected()) {
                        Files.writeString(fP, "from lintest.ios_testcase import IOSTestCase" + System.lineSeparator(), StandardOpenOption.APPEND);
                    }

                    Files.writeString(fP, System.lineSeparator() + System.lineSeparator(), StandardOpenOption.APPEND);

                    superClsNamesStr = superClsNamesStr.substring(0, superClsNamesStr.length() - 2);

                    Files.writeString(fP, "class " + fileName.split("\\.")[0] + "(" + superClsNamesStr + "):" + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "    tag = 'regression'" + System.lineSeparator() + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "    def setup(self):" + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "        self.logger.info(\"setup()...\")" + System.lineSeparator() + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "    def teardown(self):" + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "        self.logger.info(\"teardown()...\")" + System.lineSeparator() + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "    def run_test(self):" + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "        # 输入 self. 获得框架提供的基础能力" + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "        self.logger.info(\"write the business code here\")" + System.lineSeparator(), StandardOpenOption.APPEND);
                    Files.writeString(fP, "        self.requests.get(self, url=\"https://www.bing.com\")  # will auto log the request's params" + System.lineSeparator(), StandardOpenOption.APPEND);

                    if (createTestCaseFileUI.getUiCheckBox().isSelected()) {
                        Files.writeString(fP, "        self.browser.get(\"https://www.bing.com\")" + System.lineSeparator(), StandardOpenOption.APPEND);
                    }

                }

                Messages.showMessageDialog(fileName, "成功创建", Messages.getInformationIcon());
                project.getBaseDir().refresh(false, true);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        dialogBuilder.show();
    }
}
