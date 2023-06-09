package com.github.linktest;

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


public class CreateATestCaseFile extends AnAction {


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

        CreateTestCaseFileUI createTestCaseFileUI = new CreateTestCaseFileUI();
        createTestCaseFileUI.getApiCheckBox().setSelected(true);
        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(createTestCaseFileUI.getRootPanel());
        dialogBuilder.setTitle("Add a testcase file to the package");

        if (System.getProperty("os.name").startsWith("Windows")) {
            createTestCaseFileUI.setPackagePath(selectedPackagePath.replaceAll("\\\\", "."));
        } else {
            createTestCaseFileUI.setPackagePath(selectedPackagePath.replaceAll(File.separator, "."));
        }
        createTestCaseFileUI.getPackagePath().setEditable(false);
        String finalFullPath = fullPath;
        dialogBuilder.setOkOperation(() -> {
            dialogBuilder.getDialogWrapper().close(0);
            String fileName = createTestCaseFileUI.getFileName();
            fileName = fileName.trim();

            boolean generateDemoCodeFlag = createTestCaseFileUI.getYesRadioButton().isSelected();

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

            fileName += ".py";

            File f;
            if (System.getProperty("os.name").startsWith("Windows")) {
                f = new File(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + "\\" + fileName);
            } else {
                f = new File(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath() + File.separator + fileName);
            }

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
                String filePath = "";

                if (System.getProperty("os.name").startsWith("Windows")) {
                    filePath =  finalFullPath + "\\" + fileName;
                } else {
                    filePath =  finalFullPath + File.separator + fileName;
                }

                Path fP = Path.of(filePath);
                Files.createFile(fP);

                if (createTestCaseFileUI.getApiCheckBox().isSelected()) {
                    Files.writeString(fP, "from linktest.api_testcase import APITestCase" + System.lineSeparator(), StandardOpenOption.APPEND);
                }
                if (createTestCaseFileUI.getUiCheckBox().isSelected()) {
                    Files.writeString(fP, "from linktest.ui_testcase import UITestCase" + System.lineSeparator(), StandardOpenOption.APPEND);
                }
                if (createTestCaseFileUI.getAndroidCheckBox().isSelected()) {
                    Files.writeString(fP, "from linktest.android_testcase import AndroidTestCase" + System.lineSeparator(), StandardOpenOption.APPEND);
                }
                if (createTestCaseFileUI.getIosCheckBox().isSelected()) {
                    Files.writeString(fP, "from linktest.ios_testcase import IOSTestCase" + System.lineSeparator(), StandardOpenOption.APPEND);
                }

                Files.writeString(fP, System.lineSeparator() + System.lineSeparator(), StandardOpenOption.APPEND);

                superClsNamesStr = superClsNamesStr.substring(0, superClsNamesStr.length() - 2);

                Files.writeString(fP, "class " + fileName.split("\\.")[0] + "(" + superClsNamesStr + "):" + System.lineSeparator(), StandardOpenOption.APPEND);

                String codeDemoStr = "    tag = 'regression'\n" +
                        "\n" +
                        "    def setup(self):\n" +
                        "        self.logger.info(\"setup()...\")\n" +
                        "\n" +
                        "    def teardown(self):\n" +
                        "        self.logger.info(\"teardown()...\")\n" +
                        "\n" +
                        "    def run_test(self):\n" +
                        "        # 提示: 输入 self. 查看框架提供的基础能力\n" +
                        "        self.logger.info(\"write the business code here\")";

                Files.writeString(fP,codeDemoStr + System.lineSeparator(), StandardOpenOption.APPEND);


                if (generateDemoCodeFlag) {
                    String demoCodeStr = "\n        # ******** compare_json Demo ********\n" +
                            "        json1 = {\n" +
                            "            \"name\": \"test\",\n" +
                            "            \"Group\": {\n" +
                            "                \"name\": \"A\",\n" +
                            "                \"No\": 1\n" +
                            "            },\n" +
                            "        }\n" +
                            "        json2 = {\n" +
                            "            \"Group\": {\n" +
                            "                \"name\": \"A\",\n" +
                            "                \"No\": 1\n" +
                            "            },\n" +
                            "            \"name\": \"test\",\n" +
                            "        }\n" +
                            "        self.compare_json_and_assert_equal(json1, json2)\n" +
                            "        # self.compare_json_and_return_diff(json1, json2)\n" +
                            "        # self.compare_json_with_strict_mode(json1, json2)\n" +
                            "\n" +
                            "        # ******** GET Request Demo ********\n" +
                            "        self.requests.get('https://www.douban.com/')  # 不会 自动保存请求信息到log\n" +
                            "        self.requests.get(self, 'https://www.douban.com/')  # 会 自动保存请求信息到log\n\n" +
                            "        # 实际请求的URL: https://www.douban.com/search?q=HuaAn&cat=9527\n" +
                            "        self.requests.get(self, 'https://www.douban.com/search', params={'q': 'HuaAn', 'cat': '9527'})\n" +
                            "\n" +
                            "        # ******** POST Request Demo ********\n" +
                            "        headers = {\n" +
                            "            \"Content-Type\": \"application/json\",\n" +
                            "        }\n" +
                            "        post_data = {\n" +
                            "            \"form_email\": \"test@example.com\",\n" +
                            "            \"form_password\": \"test123456\"\n" +
                            "        }\n" +
                            "\n" +
                            "        # 第一个参数为self,则会自动log请求参数\n" +
                            "        res1 = self.requests.post(self,\n" +
                            "                                  \"https://accounts.douban.com/login\",\n" +
                            "                                  data=post_data,\n" +
                            "                                  headers=headers)\n" +
                            "        # assert res1.status_code == 200\n" +
                            "\n" +
                            "        # requests 默认使用 application/x-www-form-urlencoded 对 POST 数据编码.\n" +
                            "        # 如果要传递JSON数据, 可以直接传入json参数:\n" +
                            "        res2 = self.requests.post(self,\n" +
                            "                                  \"https://accounts.douban.com/login\",\n" +
                            "                                  json=post_data,  # 内部自动序列化为JSON\n" +
                            "                                  headers=headers)\n" +
                            "        # assert res2.status_code == 200\n" +
                            "\n" +
                            "        # ******** Integrate Test-Engine Demo ********\n" +
                            "        if hasattr(self.TestEngineCaseInput, \"userName\"):\n" +
                            "            print(self.TestEngineCaseInput[\"userName\"])\n" +
                            "        if hasattr(self.TestEngineCaseInput, \"password\"):\n" +
                            "            print(self.TestEngineCaseInput[\"password\"])\n" +
                            "        # tokenStr = login(self.TestEngineCaseInput[\"userName\"], self.TestEngineCaseInput[\"password\"])\n" +
                            "        self.TestEngineCaseOutput[\"Token\"] = \"tokenStr...\"\n" +
                            "\n" +
                            "        # ******** 其它 ********\n" +
                            "        self.pprint(post_data)\n" +
                            "        self.sleep(1)";

                    Files.writeString(fP, "        " + demoCodeStr + System.lineSeparator(), StandardOpenOption.APPEND);


                    if (createTestCaseFileUI.getUiCheckBox().isSelected()) {
                        Files.writeString(fP, "\n        # ******** Browser/ WebDriver ********" + System.lineSeparator(), StandardOpenOption.APPEND);
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
