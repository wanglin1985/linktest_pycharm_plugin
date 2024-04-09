package com.github.linktest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;


public class AddHttpRequestDemoCode extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取到editor和project
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (editor == null || project == null) {
            return;
        }

        SelectionModel selectionModel = editor.getSelectionModel();

        final int lineNum = selectionModel.getSelectionEndPosition().line;

        AddHttpRequestDemoCodeUI addHttpRequestDemoCodeUI = new AddHttpRequestDemoCodeUI();

        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(addHttpRequestDemoCodeUI.getRootPanel());
        dialogBuilder.setTitle("Add Http Request Template Code");
        dialogBuilder.setOkOperation(() -> {
            dialogBuilder.getDialogWrapper().close(0);

            String caseNameListInSuitFile = " ";
            String addSuccessInfo = "";

            String[] lines;
            ArrayList ClassNameLines = new ArrayList();

            if (System.getProperty("os.name").startsWith("Windows")) {
                lines = editor.getDocument().getText().split("\n");
            } else {
                lines = editor.getDocument().getText().split(System.lineSeparator());
            }

            ArrayList newLines = new ArrayList();

            for (int i = 0; i < lines.length; i++) {
                String lineContent = lines[i];
                if (lineContent.startsWith("class ") &&
                        (
                                lineContent.trim().replaceAll(" +", "").endsWith("(APITestCase):") ||
                                        lineContent.trim().replaceAll(" +", "").endsWith("(UITestCase):") ||
                                        lineContent.trim().replaceAll(" +", "").endsWith("(IOSTestCase):") ||
                                        lineContent.trim().replaceAll(" +", "").endsWith("(AndroidTestCase):") ||
                                        lineContent.trim().replaceAll(" +", "").endsWith(",APITestCase):") ||
                                        lineContent.trim().replaceAll(" +", "").endsWith(",UITestCase):") ||
                                        lineContent.trim().replaceAll(" +", "").endsWith(",IOSTestCase):") ||
                                        lineContent.trim().replaceAll(" +", "").endsWith(",AndroidTestCase):")
                        )
                ) {

                    // 合法的 linktest Case类定义, 此时自动提取出 ClassName 并追加到 caseNameListInSuitFile
                    lineContent = lineContent.trim().replace("class ", "").split("\\(")[0];
                    caseNameListInSuitFile += lineContent + " ";

                    ClassNameLines.add(i);
                }

                newLines.add(lines[i]);
            }

            if (ClassNameLines.size() >= 1) {
                String add_str = "";

                if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                    add_str += "\n        headers = {\n" +
                            "            \"Content-Type\": \"application/json\",\n" +
                            "        }\n";
                }

                if (addHttpRequestDemoCodeUI.getPOSTRadioButton().isSelected()) {
                    if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                        add_str += "\n        post_data = {\n" +
                                "            \"key\": \"<value>\",\n" +
                                "        }\n" +
                                "\n" +
                                "        self.requests.post(\"<your-api-endpoint>\",\n" +
                                "                           data=post_data,\n" +
                                "                           headers=headers)\n";
                    } else {
                        add_str += "\n        post_data = {\n" +
                                "            \"key\": \"<value>\",\n" +
                                "        }\n" +
                                "\n" +
                                "        # 会自动log请求参数\n" +
                                "        self.requests.post(\"<your-api-endpoint>\",\n" +
                                "                           data=post_data)\n";
                    }

                    addSuccessInfo = "Add POST Template Code";
                } else if (addHttpRequestDemoCodeUI.getGETRadioButton().isSelected()) {
                    if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                        add_str += "        self.requests.get('<your-api-endpoint>', headers=headers)";
                    } else {
                        add_str += "        self.requests.get('<your-api-endpoint>')";
                    }

                    addSuccessInfo = "Add GET Template Code";
                } else if (addHttpRequestDemoCodeUI.getDELETERadioButton().isSelected()) {
                    if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                        add_str += "\n        post_data = {\n" +
                                "            \"key\": \"<value>\",\n" +
                                "        }\n" +
                                "\n" +
                                "        self.requests.delete(\"<your-api-endpoint>\",\n" +
                                "                             data=post_data,\n" +
                                "                             headers=headers)\n";
                    } else {
                        add_str += "\n        post_data = {\n" +
                                "            \"key\": \"<value>\",\n" +
                                "        }\n" +
                                "\n" +
                                "        self.requests.delete(\"<your-api-endpoint>\",\n" +
                                "                             data=post_data)\n";
                    }
                    addSuccessInfo = "Add Delete Template Code";
                } else if (addHttpRequestDemoCodeUI.getPUTRadioButton().isSelected()) {
                    if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                        add_str += "\n        post_data = {\n" +
                                "            \"key\": \"<value>\",\n" +
                                "        }\n" +
                                "\n" +
                                "        self.requests.put(\"<your-api-endpoint>\",\n" +
                                "                          data=post_data,\n" +
                                "                          headers=headers)\n";
                    } else {
                        add_str += "\n        post_data = {\n" +
                                "            \"key\": \"<value>\",\n" +
                                "        }\n" +
                                "\n" +
                                "        self.requests.put(\"<your-api-endpoint>\",\n" +
                                "                          data=post_data)\n";
                    }
                    addSuccessInfo = "Add Put Template Code";
                }

                // write the add_str to selected file
                if (lineNum == lines.length) {
                    newLines.add(lineNum, add_str);
                } else {
                    if (lines[lineNum].replaceAll(" +", "").length() > 0) {
                        newLines.add(lineNum + 1, add_str);
                    } else {
                        newLines.add(lineNum, add_str);
                    }
                }

            } else {
                String add_str = "";

                if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                    add_str += "\n    headers = {\n" +
                            "        \"Content-Type\": \"application/json\",\n" +
                            "    }\n";
                }

                if (addHttpRequestDemoCodeUI.getPOSTRadioButton().isSelected()) {
                    if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                        add_str += "\n    post_data = {\n" +
                                "        \"key\": \"<value>\",\n" +
                                "    }\n" +
                                "\n" +
                                "    self.requests.post(\"<your-api-endpoint>\",\n" +
                                "                             data=post_data,\n" +
                                "                             headers=headers)\n";
                    } else {
                        add_str += "\n    post_data = {\n" +
                                "        \"key\": \"<value>\",\n" +
                                "    }\n" +
                                "\n" +
                                "    self.requests.post(\"<your-api-endpoint>\",\n" +
                                "                             data=post_data)\n";
                    }

                    addSuccessInfo = "Add POST Template Code";
                } else if (addHttpRequestDemoCodeUI.getDELETERadioButton().isSelected()) {
                    if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                        add_str += "\n    post_data = {\n" +
                                "        \"key\": \"<value>\",\n" +
                                "    }\n" +
                                "\n" +
                                "    self.requests.delete(\"<your-api-endpoint>\",\n" +
                                "                               data=post_data,\n" +
                                "                               headers=headers)\n";
                    } else {
                        add_str += "\n    post_data = {\n" +
                                "        \"key\": \"<value>\",\n" +
                                "    }\n" +
                                "\n" +
                                "    self.requests.delete(\"<your-api-endpoint>\",\n" +
                                "                               data=post_data)\n";
                    }
                    addSuccessInfo = "Add Delete Template Code";
                } else if (addHttpRequestDemoCodeUI.getPUTRadioButton().isSelected()) {
                    if (addHttpRequestDemoCodeUI.getYESRadioButton().isSelected()) {
                        add_str += "\n    post_data = {\n" +
                                "        \"key\": \"<value>\",\n" +
                                "    }\n" +
                                "\n" +
                                "    self.requests.put(\"<your-api-endpoint>\",\n" +
                                "                            data=post_data,\n" +
                                "                            headers=headers)\n";
                    } else {
                        add_str += "\n    post_data = {\n" +
                                "        \"key\": \"<value>\",\n" +
                                "    }\n" +
                                "\n" +
                                "    self.requests.put(\"<your-api-endpoint>\",\n" +
                                "                            data=post_data)\n";
                    }
                    addSuccessInfo = "Add Put Template Code";
                }

                // write the add_str to selected file
                if (lineNum == lines.length) {
                    newLines.add(lineNum, add_str);
                } else {
                    if (lines[lineNum].replaceAll(" +", "").length() > 0) {
                        newLines.add(lineNum + 1, add_str);
                    } else {
                        newLines.add(lineNum, add_str);
                    }
                }

            }
//            } else {
//                Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
//                        "该文件中没有找到有效的TestCase:", IconLoader.getIcon("/icons/sdk_16.svg", SdkIcons.class));
//                return;
//            }

            //  write newLines to case file
            try {
                String filePath = ((EditorImpl) editor).getVirtualFile().getPath();
                Path fP = Path.of(filePath);
                Files.delete(fP);
                Files.createFile(fP);

                if (System.getProperty("os.name").startsWith("Windows")) {
                    for (int i = 0; i < newLines.size(); i++) {
                        Files.writeString(fP, newLines.get(i) + "\n", StandardOpenOption.APPEND);
                    }
                } else {
                    for (int i = 0; i < newLines.size(); i++) {
                        Files.writeString(fP, newLines.get(i) + System.lineSeparator(), StandardOpenOption.APPEND);
                    }
                }

                System.out.println(addSuccessInfo);
                System.out.println(Messages.getInformationIcon());
                Messages.showMessageDialog(addSuccessInfo, "Success", Messages.getInformationIcon());
                project.getBaseDir().refresh(true, true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        dialogBuilder.show();
    }
}
