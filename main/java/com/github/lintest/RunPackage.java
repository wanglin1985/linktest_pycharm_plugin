package com.github.lintest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.IOException;


public class RunPackage extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取到editor和project
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getData(CommonDataKeys.PROJECT);
        Navigatable navigatable = e.getData(CommonDataKeys.NAVIGATABLE);

        String selectedPackagePath = navigatable.toString();

        selectedPackagePath = selectedPackagePath.split(project.getBasePath())[1];
        selectedPackagePath = selectedPackagePath.replaceAll(File.separator, ".");
        if (selectedPackagePath.startsWith(".")) {
            selectedPackagePath = selectedPackagePath.replaceFirst(".", "");
        }
        System.out.println(selectedPackagePath);

        if (selectedPackagePath == null || !selectedPackagePath.startsWith("tests.")) {
            System.out.println("package is invalid!");
            System.out.println("package is invalid!");
            System.out.println("package is invalid!");
            Messages.showMessageDialog(project, selectedPackagePath, "不合法的包路径:", Messages.getErrorIcon());

            return;
        }

        TerminalView terminalView = TerminalView.getInstance(project);
        String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " "  + selectedPackagePath;
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
