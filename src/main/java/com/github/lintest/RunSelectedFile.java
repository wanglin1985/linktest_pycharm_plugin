package com.github.lintest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.pom.Navigatable;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class RunSelectedFile extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }

        Navigatable navigatable = e.getData(CommonDataKeys.NAVIGATABLE);
        String selectedPackagePath = navigatable.toString();
        String fullPath = "";

        if (selectedPackagePath.startsWith("PsiDirectory:")) {
            Messages.showMessageDialog("请选择一个 .py 文件", "错误", Messages.getErrorIcon());
            return;
        } else {
            fullPath = e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath();
        }

        String caseNameListInSuitFile = " ";
        List<String> lines;

        Path fP = Path.of(fullPath);
        try {
            lines = Files.readAllLines(fP);
            for (String lineContent : lines) {
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

                    // 合法的 lintest Case类定义, 此时自动提取出 ClassName 并追加到 caseNameListInSuitFile
                    lineContent = lineContent.trim().replace("class ", "").split("\\(")[0].replaceAll(" +", "");
                    caseNameListInSuitFile += lineContent + " ";
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if (" ".equals(caseNameListInSuitFile)) {
            Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                    "该文件中没有找到有效的TestCase:", IconLoader.getIcon("/icons/sdk_16.svg", SdkIcons.class));
            return;
        }

        TerminalView terminalView = TerminalView.getInstance(project);
        String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " " + caseNameListInSuitFile;

        try {
            ShellTerminalWidget shellTerminalWidget = terminalView.createLocalShellWidget(project.getBasePath(), "RunTest");
            shellTerminalWidget.executeCommand("****** Start to run testcase file: " + fullPath);
            shellTerminalWidget.executeCommand(command);
        } catch (IOException err) {
            err.printStackTrace();
        }

    }

}
