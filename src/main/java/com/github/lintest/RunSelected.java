package com.github.lintest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.IOException;


public class RunSelected extends AnAction {


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

        // 拿到选中部分字符串
        // todo: 此处应该enhance一下,判断选择的当前行是否包含了 （APITestCase/UITestCase/BaseTestCase),否则不是 case???
        String selectedText = selectionModel.getSelectedText();

        if (selectedText != null) {
            System.out.println(selectedText);
            if (selectedText.trim().startsWith("class ") && (selectedText.trim().endsWith("(APITestCase):") ||
                    selectedText.trim().endsWith("(UITestCase):") || selectedText.trim().endsWith("(IOSTestCase):") ||
                    selectedText.trim().endsWith("(AndroidTestCase):"))) {
                // 合法的 lintest Case类定义, 此时自动提取出 ClassName
                selectedText = selectedText.trim().replace("class ", "").split("\\(")[0];
            }
        }

        TerminalView terminalView = TerminalView.getInstance(project);
        String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " " + selectedText;

        try {
            terminalView.createLocalShellWidget(project.getBasePath(), "LinTestRun").executeCommand(command);
        } catch (IOException err) {
            err.printStackTrace();
        }

    }

}
