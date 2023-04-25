package com.github.linktest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.IOException;


public class RunSuite extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        e.getPresentation().setIcon(SdkIcons.Sdk_default_icon);

        // 获取到editor和project
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (editor == null || project == null) {
            return;
        }

        String caseNameListInSuitFile = " ";
        String[] lines;
        if (System.getProperty("os.name").startsWith("Windows")) {
            lines = editor.getDocument().getText().split("\n");
        } else {
            lines = editor.getDocument().getText().split(System.lineSeparator());
        }

        for (int i = 0; i < lines.length; i++) {
            String lineContent = lines[i];
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
                caseNameListInSuitFile += lineContent + " ";
            }
        }

        if (" ".equals(caseNameListInSuitFile)) {
            Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                    "该文件中没有找到有效的TestCase:", IconLoader.getIcon("/icons/sdk_16.svg", SdkIcons.class));
            return;
        }

        TerminalView terminalView = TerminalView.getInstance(project);
        String command = "python3 " + project.getBasePath() + File.separator + "run.py" + " " + caseNameListInSuitFile;

        try {
            terminalView.createLocalShellWidget(project.getBasePath(), "RunTest").executeCommand(command);
        } catch (IOException err) {
            err.printStackTrace();
        }

    }

}
