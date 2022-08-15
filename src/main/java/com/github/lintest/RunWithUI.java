package com.github.lintest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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

        TestRunWithInputs testRunWithInputs = new TestRunWithInputs();

        // 获取光标选中文本段对象和doc对象
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        System.out.println(selectedText);

        if (selectedText != null) {
            System.out.println(selectedText);
            if (selectedText.trim().startsWith("class ") && (selectedText.trim().endsWith("(APITestCase):") ||
                    selectedText.trim().endsWith("(UITestCase):") || selectedText.trim().endsWith("(IOSTestCase):") ||
                    selectedText.trim().endsWith("(AndroidTestCase):"))) {
                // 合法的 lintest Case类定义, 此时自动提取出 ClassName 并赋值给 searchText输入框
                testRunWithInputs.setInputText(
                        selectedText.trim().replace("class ", "").split("\\(")[0]
                );
            }
            // todo: 合理的 package path
            else {
                testRunWithInputs.setInputText(selectedText);
            }

        }


        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(testRunWithInputs.getRootPanel());
        dialogBuilder.setTitle("Please input tag/caseId/packageName env threads:");
        dialogBuilder.setOkOperation(() -> {
            System.out.println(testRunWithInputs.getInputText());
            System.out.println(testRunWithInputs.getEnv());
            System.out.println(testRunWithInputs.getThreadCount());

            dialogBuilder.getDialogWrapper().close(0);

            TerminalView terminalView = TerminalView.getInstance(project);
            String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " " +
                    testRunWithInputs.getInputText() + " env=" + testRunWithInputs.getEnv() +
                    " threads=" + testRunWithInputs.getThreadCount();
//            System.out.println(command);

            try {
                terminalView.createLocalShellWidget(project.getBasePath(), "LinTestRun").executeCommand(command);
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        dialogBuilder.show();
    }

}
