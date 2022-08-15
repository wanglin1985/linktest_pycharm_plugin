package com.github.lintest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.IOException;


public class RunCase extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        e.getPresentation().setIcon(SdkIcons.Sdk_default_icon);

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
        TerminalView terminalView = TerminalView.getInstance(project);
        String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " " + selectedText;

        try {
            terminalView.createLocalShellWidget(project.getBasePath(), "LinTestRun").executeCommand(command);
        } catch (IOException err) {
            err.printStackTrace();
        }

    }

}
