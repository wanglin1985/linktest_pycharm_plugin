package com.github.lintest;

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


public class RunSuit extends AnAction {


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
        String fileContent = "";
        fileContent = editor.getDocument().getText();

        String caseNameListInSuitFile = " ";

        for (int i = 0; i < fileContent.split(System.getProperty("line.separator")).length; i++) {
            String lineContent = fileContent.split(System.getProperty("line.separator"))[i];
            if (lineContent.trim().startsWith("class ") && (lineContent.trim().endsWith("(APITestCase):") ||
                    lineContent.trim().endsWith("(UITestCase):") || lineContent.trim().endsWith("(IOSTestCase):") ||
                    lineContent.trim().endsWith("(AndroidTestCase):"))) {

                // 合法的 lintest Case类定义, 此时自动提取出 ClassName 并追加到 caseNameListInSuitFile
                lineContent = lineContent.trim().replace("class ", "").split("\\(")[0];
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
            terminalView.createLocalShellWidget(project.getBasePath(), "LinTestRun").executeCommand(command);
        } catch (IOException err) {
            err.printStackTrace();
        }

    }

}
