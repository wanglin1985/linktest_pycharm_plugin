package com.github.linktest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.IOException;


public class RunWithUI extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取到editor和project
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (editor == null || project == null) {
            return;
        }

        // 获取光标选中文本段对象和doc对象
        SelectionModel selectionModel = editor.getSelectionModel();

        Document document = editor.getDocument();

        // 拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();

        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();
        int startLineNumber = document.getLineNumber(startOffset);
        int curLineNumber = document.getLineNumber(endOffset);

        String[] lines;

        if (System.getProperty("os.name").startsWith("Windows")) {
            lines = editor.getDocument().getText().split("\n");
        } else {
            lines = editor.getDocument().getText().split(System.lineSeparator());
        }

        int lineNum = startLineNumber;

        String caseId = "";
        String tagStr = "";

        int i_in_selected_text_lines = 0;

        while (lineNum <= curLineNumber) {
            String lineContent = "";
            try {
                lineContent = lines[lineNum];
            } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                break;
            }

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

                // 合法的 linktest Case类定义, 此时自动提取出 ClassName
                caseId += lineContent.trim().replace("class ", "").split("\\(")[0] + ",";

            } else if (lineContent.replaceAll(" ", "").startsWith("tag=")) {

                lineContent = lineContent.trim().replaceAll(" ", "").
                        replace("tag=", "").replaceAll("'", "").
                        replaceAll("\"", "");

                String[] tagsInLine = lineContent.trim().replace("[", "").replace("]", "").split(",");

                String selectedTagName = "";
                if (selectedText != null) {
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        selectedTagName = selectedText.split("\n")[i_in_selected_text_lines];
                    } else {
                        selectedTagName = selectedText.split(System.lineSeparator())[i_in_selected_text_lines];
                    }
                } else {
                    selectedTagName = lineContent;
                }
                selectedTagName = selectedTagName.replaceAll("'", "").
                        replace("[", "").replace("]", "").
                        replaceAll("\"", "").replaceAll(" ", "").
                        replace("tag=", "");

                String[] selectedTagNameList = new String[0];
                selectedTagNameList = selectedTagName.split(",");

                for (int i = 0; i < selectedTagNameList.length; i++) {
                    for (int j = 0; j < tagsInLine.length; j++) {
                        if (tagsInLine[j].trim().equals(selectedTagNameList[i].trim())) {
                            tagStr += tagsInLine[j].trim() + ",";
                            break;
                        }
                    }
                }

                if (tagStr.length() > 0) {
                    tagStr = tagStr.substring(0, tagStr.length() - 1);
                }
            }

            lineNum++;
            i_in_selected_text_lines++;
        }


        if (caseId.length() > 0) {
            caseId = caseId.substring(0, caseId.length() - 1);
        } else if (tagStr.length() > 0) {
            // 没有找到 合法的case 类定义， 但是找到了 合法的  tagName, 如果找到了 合法的类定义，则会 忽略 tageName
            caseId = tagStr;
        }


        TestRunWithInputs testRunWithInputs = new TestRunWithInputs();

        testRunWithInputs.setInputText(caseId);

        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(testRunWithInputs.getRootPanel());
        dialogBuilder.setTitle("Please specify the test configuration parameters");

        dialogBuilder.setOkOperation(() -> {
            dialogBuilder.getDialogWrapper().close(0);

            String reRunFlagStrForPython = "True";
            if (testRunWithInputs.getRetryFailedYesRadioButton().isSelected()) {
                System.out.println("Retry Failed is selected");
                reRunFlagStrForPython = "True";
            } else {
                System.out.println("Retry Failed is not selected");
                reRunFlagStrForPython = "False";
            }

            String autoScreenshotOnActionStrForPython = "False";
            if (testRunWithInputs.getYesAutoScreenshotOnAction().isSelected()) {
                System.out.println("Auto Screenshot on Action is selected");
                autoScreenshotOnActionStrForPython = "True";
            } else {
                System.out.println("Auto Screenshot on Action is not selected");
                autoScreenshotOnActionStrForPython = "False";
            }

            String logToFileCheckBoxForPython = "False";
            if (testRunWithInputs.getFileCheckBox().isSelected()) {
                logToFileCheckBoxForPython = "True";
            } else {
                logToFileCheckBoxForPython = "False";
            }

            String logToConsoleCheckBoxForPython = "False";
            if (testRunWithInputs.getConsoleCheckBox().isSelected()) {
                logToConsoleCheckBoxForPython = "True";
            } else {
                logToConsoleCheckBoxForPython = "False";
            }

            String inputCaseId = testRunWithInputs.getInputText();

            inputCaseId = inputCaseId.trim().replaceAll(" +", ",").replaceAll(",+", ",");

            TerminalView terminalView = TerminalView.getInstance(project);
            String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " case_id=" +
                    inputCaseId + " env=" + testRunWithInputs.getEnv() +
                    " threads=" + testRunWithInputs.getThreadCount() + " rerun_flag=" + reRunFlagStrForPython +
                    " auto_screenshot_on_action=" + autoScreenshotOnActionStrForPython +
                    " log_to_file=" + logToFileCheckBoxForPython + " log_to_console=" + logToConsoleCheckBoxForPython;

            try {
                terminalView.createLocalShellWidget(project.getBasePath(), "RunTest").executeCommand(command);
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        dialogBuilder.show();
    }

}
