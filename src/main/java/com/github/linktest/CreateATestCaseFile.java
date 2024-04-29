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
                    Messages.showMessageDialog("1. File names must consist only of lowercase letters, digits, and underscores, and must begin with a letter. \n文件名应仅由小写字母、数字和下划线组成，并需以字母开头。 \n\t2.The file extension must be .py or absent. \n\t文件扩展名仅限于 .py 或无扩展名。\n", "Invalid file Name - 无效的文件名", Messages.getErrorIcon());
                    return;
                }

                fileName = fileName.substring(0, fileName.length() - 3);
            }

            String pyFileRegx = "^[a-z]+[_a-z0-9]*$";
            Pattern pattern = Pattern.compile(pyFileRegx);

            if (!pattern.matcher(fileName).find()) {
                Messages.showMessageDialog("1. File names must consist only of lowercase letters, digits, and underscores, and must begin with a letter. \n文件名应仅由小写字母、数字和下划线组成，并需以字母开头。\n\t 2.The file extension must be .py or absent. \n\t文件扩展名仅限于 .py 或无扩展名。\n", "Invalid file Name - 无效的文件名", Messages.getErrorIcon());
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
                Messages.showMessageDialog("File: " + fileName + " already exists in the current directory.", "Error", Messages.getErrorIcon());
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



                Files.writeString(fP, "class " + CapWordsConverter.toCapWords(fileName.split("\\.")[0], "_") + "(" + superClsNamesStr + "):" + System.lineSeparator(), StandardOpenOption.APPEND);

                String codeDemoStr = "    tag = 'regression'\n" +
                        "\n" +
                        "    def setup(self):\n" +
                        "        self.logger.info(\"setup()...\")\n" +
                        "\n" +
                        "    def teardown(self):\n" +
                        "        self.logger.info(\"teardown()...\")\n" +
                        "\n" +
                        "    def run_test(self):\n" +
                        "        # Tips:\n" +
                        "        # 1. Type self. to explore the foundational capabilities provided by the framework.\n" +
                        "        # 2. The framework will automatically capture and log all request information for both HTTP and UI interactions.\n" +
                        "        self.logger.info(\"Insert business logic here...\")";

                Files.writeString(fP,codeDemoStr + System.lineSeparator(), StandardOpenOption.APPEND);


                if (generateDemoCodeFlag) {
                    if (createTestCaseFileUI.getUiCheckBox().isSelected()) {
                        Files.writeString(fP, "\n        # ******** UI Test Case Example ********" + System.lineSeparator(), StandardOpenOption.APPEND);
                        Files.writeString(fP, "        self.browser.get(\"https://www.bing.com\")" + System.lineSeparator(), StandardOpenOption.APPEND);
                        Files.writeString(fP, "        self.browser.find_element(\"xpath\", '//*[@id=\"sb_form_q\"]').click()" + System.lineSeparator(), StandardOpenOption.APPEND);
                        Files.writeString(fP, "        self.browser.find_element(\"xpath\", '//*[@id=\"sb_form_q\"]').send_keys(\"linktest PyCharm Plugin\")\n" + System.lineSeparator(), StandardOpenOption.APPEND);
                        Files.writeString(fP, "        from selenium.webdriver import Keys" + System.lineSeparator(), StandardOpenOption.APPEND);
                        Files.writeString(fP, "        self.browser.find_element(\"xpath\", '//*[@id=\"sb_form_q\"]').send_keys(Keys.ENTER)" + System.lineSeparator(), StandardOpenOption.APPEND);
                    }

                    String demoCodeStr = "\n" +
                            "        # ******** HTTP GET Request Example ********\n" +
                            "        self.requests.get('https://jsonplaceholder.typicode.com/posts/1') \n\n" +
                            "        # ******** HTTP GET Request Example with parameters ********\n" +
                            "        params = {\n" +
                            "            'userId': 1\n" +
                            "        }\n" +
                            "        response = self.requests.get(\"https://jsonplaceholder.typicode.com/posts\", params=params)\n" +
                            "        self.assert_equals(response.status_code, 200)\n" +
                            "\n" +
                            "        # ******** HTTP POST Request Example ********\n" +
                            "        headers = {\n" +
                            "            \"Content-Type\": \"application/json\",\n" +
                            "        }\n" +
                            "        post_data = {\n" +
                            "            \"title\": \"Demo HTTP POST Request\",\n" +
                            "            \"body\": \"This is a demonstration of an HTTP POST request payload.\"\n" +
                            "        }\n" +
                            "\n" +
                            "        res = self.requests.post(\"https://jsonplaceholder.typicode.com/posts\",\n" +
                            "                                 json=post_data,\n" +
                            "                                 headers=headers)\n" +
                            "        self.assert_equals(res.status_code, 201)\n" +
                            "\n        # ******** JSON Comparison Example ********\n" +
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
                            "        # ******** Integrate Test-Engine Example ********\n" +
                            "        if hasattr(self.TestEngineCaseInput, \"userName\"):\n" +
                            "            print(self.TestEngineCaseInput[\"userName\"])\n" +
                            "        if hasattr(self.TestEngineCaseInput, \"password\"):\n" +
                            "            print(self.TestEngineCaseInput[\"password\"])\n" +
                            "        self.TestEngineCaseOutput[\"Token\"] = \"tokenStr...\"\n" +
                            "\n" +
                            "        # ******** other ********\n" +
                            "        self.pprint(post_data)\n" +
                            "        self.sleep(1)";

                    Files.writeString(fP, "        " + demoCodeStr + System.lineSeparator(), StandardOpenOption.APPEND);
                }

                Messages.showMessageDialog(fileName, "Creation successful", Messages.getInformationIcon());
                project.getBaseDir().refresh(false, true);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        dialogBuilder.show();
    }
}
