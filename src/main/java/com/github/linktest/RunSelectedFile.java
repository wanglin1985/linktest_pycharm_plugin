package com.github.linktest;

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
import com.intellij.openapi.vfs.VirtualFile;


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

        String projectBasePath = project.getBasePath();
        String caseWithFullPackage = "";

        // 获取当前操作的文件
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file != null) {
            // 获取文件的完整路径
            String filePath = file.getPath();

            // projectBasePath 后面加上 File.separator
            projectBasePath += File.separator;

            // 去掉 .py 后缀，使用 length - 3
            caseWithFullPackage = filePath.replace(projectBasePath, "").substring(0, filePath.replace(projectBasePath, "").length() - 3).replaceAll(File.separator, ".");
        } else {
            return;
        }

        Navigatable navigatable = e.getData(CommonDataKeys.NAVIGATABLE);
        String selectedPackagePath = navigatable.toString();
        String fullPath = "";

        if (selectedPackagePath.startsWith("PsiDirectory:")) {
            Messages.showMessageDialog("Choose a .py file please.", "Error", Messages.getErrorIcon());
            return;
        } else {
            fullPath = e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath();
        }

        String caseNameListInSuitFile = " ";
        String tagNameListInDataDrivenCaseFile = " ";
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

                    // 合法的 linktest Case类定义, 此时自动提取出 ClassName 并追加到 caseNameListInSuitFile
                    lineContent = lineContent.trim().replace("class ", "").split("\\(")[0].replaceAll(" +", "");
                    lineContent = caseWithFullPackage + "." + lineContent;
                    caseNameListInSuitFile += lineContent + " ";
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if (" ".equals(caseNameListInSuitFile)) {

            try {
                lines = Files.readAllLines(fP);
                int def_run_test_count = 0;
                for (String lineContent : lines) {
                    if (lineContent.startsWith("def run_test(")) {
                        // 合法的 linktest dataDriven Case类定义, 此时自动提取出 tagName 并追加到 tagNameListInDataDrivenCaseFile
                        def_run_test_count += 1;
                    }
                    if (lineContent.replaceAll(" ", "").startsWith("tag=")) {
                        tagNameListInDataDrivenCaseFile += lineContent.trim().split("=")[1].replace("\"", "") + " ";
                    }
                }

                if (def_run_test_count > 1) {
                    Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                            "Multiple def run_test methods have been identified, violating the LinkTest specifications.", IconLoader.getIcon("/icons/sdk_16.svg", SdkIcons.class));
                    return;
                }

                if (def_run_test_count == 1 && tagNameListInDataDrivenCaseFile.length() > 0) {
                    TerminalView terminalView = TerminalView.getInstance(project);
                    String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " " + tagNameListInDataDrivenCaseFile;

                    try {
                        ShellTerminalWidget shellTerminalWidget = terminalView.createLocalShellWidget(project.getBasePath(), "RunTest");
                        shellTerminalWidget.executeCommand("****** Start to run testcase file: " + fullPath);
                        shellTerminalWidget.executeCommand(command);
                    } catch (IOException err) {
                        err.printStackTrace();
                    }

                    return;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                    "No Valid Test Case detected in this file:", IconLoader.getIcon("/icons/sdk_16.svg", SdkIcons.class));
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
