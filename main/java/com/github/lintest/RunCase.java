package com.github.lintest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.IOException;


public class RunCase extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取到editor和project
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (editor == null || project == null) {
            return;
        }
        System.out.println(editor);
//        String projectPath
        // 获取光标选中文本段对象和doc对象
        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();
        System.out.println(document);
        System.out.println(document.getClass());
        System.out.println("selectionModel");


//        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
//        VirtualFile vFile = psiFile.getOriginalFile().getVirtualFile();
//        String path = vFile.getPath();
//        System.out.println("path:");
//        System.out.println(path);
//        path = path.toString();
//        System.out.println(path);

        // 拿到选中部分字符串
        // todo: 此处应该enhance一下,判断选择的当前行是否包含了 （APITestCase/UITestCase/BaseTestCase),否则不是 case???

        String selectedText = selectionModel.getSelectedText();
        System.out.println(selectedText);

        TerminalView terminalView = TerminalView.getInstance(project);
//        String command = "python3 /Users/liwan76/workspace/test/test-engine-demo/run.py Testcase1";
//        String command = "python3 " + path + File.separator + "run.py" + " "  + selectedText;
        String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " "  + selectedText;
        System.out.println(command);
        System.out.println(command);
        System.out.println(command);
        System.out.println(command);
        System.out.println(command);

        try {
            terminalView.createLocalShellWidget(project.getBasePath(), "LinTest Run").executeCommand(command);
        } catch (IOException err) {
            err.printStackTrace();
        }


    }

}
