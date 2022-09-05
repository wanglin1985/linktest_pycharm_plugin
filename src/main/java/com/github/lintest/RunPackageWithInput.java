package com.github.lintest;

import com.intellij.execution.dashboard.actions.DebugAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class RunPackageWithInput extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取到editor和project
        Project project = e.getData(CommonDataKeys.PROJECT);

        Navigatable navigatable = e.getData(CommonDataKeys.NAVIGATABLE);

        String selectedPackagePath = navigatable.toString();

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
        } else {
            Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                    "不合法的包路径:", Messages.getErrorIcon());
            return;
        }

        if (selectedPackagePath == null || !selectedPackagePath.startsWith("tests.")) {
            Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                    "不合法的包路径:", Messages.getErrorIcon());
            return;
        }

        TestRunWithInputs testRunWithInputs = new TestRunWithInputs();

        testRunWithInputs.setInputText(selectedPackagePath);

        DialogBuilder dialogBuilder = new DialogBuilder(project);

        dialogBuilder.setCenterPanel(testRunWithInputs.getRootPanel());
        dialogBuilder.setTitle("Please enter the startup parameters");
        dialogBuilder.setOkOperation(() -> {
            dialogBuilder.getDialogWrapper().close(0);
            TerminalView terminalView = TerminalView.getInstance(project);
            String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " " +
                    testRunWithInputs.getInputText() + " env=" + testRunWithInputs.getEnv() +
                    " threads=" + testRunWithInputs.getThreadCount();
            try {
                terminalView.createLocalShellWidget(project.getBasePath(), "RunTest").executeCommand(command);
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        dialogBuilder.show();

    }
}
