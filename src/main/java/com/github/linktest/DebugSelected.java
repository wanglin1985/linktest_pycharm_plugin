package com.github.linktest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.python.run.PythonConfigurationType;
import com.jetbrains.python.run.PythonRunConfiguration;

import java.io.File;


public class DebugSelected extends AnAction {


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

        Document document = editor.getDocument();

        // 拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();

        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();
        int startLineNumber = document.getLineNumber(startOffset);
        int curLineNumber = document.getLineNumber(endOffset);

        String[] lines;

        if (System.getProperty("os.name").startsWith("Windows")) {
            lines = editor.getDocument().getText().split("\n");
        } else {
            lines = editor.getDocument().getText().split(System.lineSeparator());
        }

        int lineNum = startLineNumber;

        String caseId = "";
        String tagStr = "";

        int i_in_selected_text_lines = 0;

        while (lineNum <= curLineNumber) {
            String lineContent = "";
            try {
                lineContent = lines[lineNum];
            } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                break;
            }

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

                // 合法的 linktest Case类定义, 此时自动提取出 ClassName
                caseId += lineContent.trim().replace("class ", "").split("\\(")[0] + ",";

            } else if (lineContent.replaceAll(" ", "").startsWith("tag=")) {

                lineContent = lineContent.trim().replaceAll(" ", "").
                        replace("tag=", "").replaceAll("'", "").
                        replaceAll("\"", "");

                String[] tagsInLine = lineContent.trim().replace("[", "").replace("]", "").split(",");

                String selectedTagName = "";
                if (selectedText != null) {
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        selectedTagName = selectedText.split("\n")[i_in_selected_text_lines];
                    } else {
                        selectedTagName = selectedText.split(System.lineSeparator())[i_in_selected_text_lines];
                    }
                } else {
                    selectedTagName = lineContent;
                }

                selectedTagName = selectedTagName.replaceAll("'", "").
                        replace("[", "").replace("]", "").
                        replaceAll("\"", "").replaceAll(" ", "").
                        replace("tag=", "");

                String[] selectedTagNameList = new String[0];
                selectedTagNameList = selectedTagName.split(",");

                for (int i = 0; i < selectedTagNameList.length; i++) {
                    for (int j = 0; j < tagsInLine.length; j++) {
                        if (tagsInLine[j].trim().equals(selectedTagNameList[i].trim())) {
                            tagStr += tagsInLine[j].trim() + ",";
                            break;
                        }
                    }
                }

                if (tagStr.length() > 0) {
                    tagStr = tagStr.substring(0, tagStr.length() - 1);
                }
            }

            lineNum++;
            i_in_selected_text_lines++;
        }

        if (caseId.length() > 0) {
            caseId = caseId.substring(0, caseId.length() - 1);
            System.out.println(caseId);
        } else if (tagStr.length() > 0) {
            // 没有找到 合法的case 类定义， 但是找到了 合法的  tagName, 如果找到了 合法的类定义，则会 忽略 tageName
            caseId = tagStr;
        } else {
            if (selectedText != null) {
                Messages.showMessageDialog(project, selectedText, "选择的内容中没有找到合法的TestCase/tag", Messages.getErrorIcon());
            } else {
                Messages.showMessageDialog(project, lines[curLineNumber], "光标所在行的内容中没有找到合法的TestCase/tag", Messages.getErrorIcon());
            }
            return;
        }

        String runScriptPath = project.getBasePath() + File.separator + "run.py";

        // 获取 Python 运行配置类型
        ConfigurationFactory configurationFactory = PythonConfigurationType.getInstance().getConfigurationFactories()[0];

        // 创建一个新的运行配置
        RunnerAndConfigurationSettings runSettings = RunManager.getInstance(project).createConfiguration("Debug Run", configurationFactory);
        runSettings.setName("Debug Run");

        // 设置运行配置的参数
        PythonRunConfiguration runConfiguration = (PythonRunConfiguration) runSettings.getConfiguration();
        runConfiguration.setScriptName(runScriptPath);
        runConfiguration.setScriptParameters(caseId);

        // 添加运行配置到 RunManager
        RunManager.getInstance(project).addConfiguration(runSettings);
        RunManager.getInstance(project).setSelectedConfiguration(runSettings);

        try {
            // 使用 ExecutionEnvironmentBuilder 启动调试会话
            ExecutionEnvironmentBuilder environmentBuilder = ExecutionEnvironmentBuilder.create(DefaultDebugExecutor.getDebugExecutorInstance(), runSettings);
            environmentBuilder.buildAndExecute();
        } catch (ExecutionException de) {
            de.printStackTrace();
            Messages.showErrorDialog("Failed to start debugging session", "Debugging Error");
        }

    }

}
