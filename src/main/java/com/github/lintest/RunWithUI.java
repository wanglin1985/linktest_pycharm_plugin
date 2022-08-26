package com.github.lintest;

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

        String[] lines = editor.getDocument().getText().split(System.lineSeparator());
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

            if (lineContent.trim().startsWith("class ") && (lineContent.trim().endsWith("(APITestCase):") ||
                    lineContent.trim().endsWith("(UITestCase):") || lineContent.trim().endsWith("(IOSTestCase):") ||
                    lineContent.trim().endsWith("(AndroidTestCase):"))) {
                // 合法的 lintest Case类定义, 此时自动提取出 ClassName
                caseId += lineContent.trim().replace("class ", "").split("\\(")[0] + ",";
            } else if (lineContent.replaceAll(" ", "").startsWith("tag=")) {
                lineContent = lineContent.trim().replaceAll(" ", "").
                        replace("tag=", "").replaceAll("'", "").
                        replaceAll("\"", "");

                String[] tagsInLine = lineContent.trim().split(",");
                String selectedTagName = "";
                if (selectedText != null) {
                    selectedTagName = selectedText.split(System.lineSeparator())[i_in_selected_text_lines];
                } else {
                    selectedTagName = lineContent;
                }
                selectedTagName = selectedTagName.replaceAll("'", "").
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
        dialogBuilder.setTitle("Please enter the startup parameters");
        dialogBuilder.setOkOperation(() -> {
            dialogBuilder.getDialogWrapper().close(0);

            String inputCaseId = testRunWithInputs.getInputText();
            inputCaseId = inputCaseId.trim().replaceAll(" +", ",").replaceAll(",+", ",");

            TerminalView terminalView = TerminalView.getInstance(project);
            String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " case_id=" + inputCaseId
                    + " env=" + testRunWithInputs.getEnv() + " threads=" + testRunWithInputs.getThreadCount();

            try {
                terminalView.createLocalShellWidget(project.getBasePath(), "RunTest").executeCommand(command);
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        dialogBuilder.show();
    }

}
