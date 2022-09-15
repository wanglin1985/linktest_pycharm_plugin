package com.github.lintest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;


public class SetTagsByFile extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取到editor和project
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (editor == null || project == null) {
            return;
        }

        SetTagsUI setTagsUI = new SetTagsUI();

        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(setTagsUI.getRootPanel());
        dialogBuilder.setTitle("请输入 tagName");
        dialogBuilder.setOkOperation(() -> {
            dialogBuilder.getDialogWrapper().close(0);

            String inputTagName = setTagsUI.getTagName();

            inputTagName = inputTagName.trim().replaceAll(" +", "").replaceAll(",+", ",").replaceAll("，", ",").replaceAll("\\[", "").replaceAll("]", "");
            if (inputTagName.startsWith(",")) {
                inputTagName = inputTagName.replaceFirst(",", "");
            }

            if (inputTagName.endsWith(",")) {
                inputTagName = inputTagName.substring(0, inputTagName.length() - 1);
            }

            if (inputTagName.length() == 0) {
                Messages.showMessageDialog("tagName 不能为空", "tagName 不合法", Messages.getErrorIcon());
                return;
            }

            String caseNameListInSuitFile = " ";
            String[] lines;
            ArrayList ClassNameLines = new ArrayList();

            if (System.getProperty("os.name").startsWith("Windows")) {
                lines = editor.getDocument().getText().split("\n");
            } else {
                lines = editor.getDocument().getText().split(System.lineSeparator());
            }

            String[] newLines = new String[lines.length];

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

                    // 合法的 lintest Case类定义, 此时自动提取出 ClassName 并追加到 caseNameListInSuitFile
                    lineContent = lineContent.trim().replace("class ", "").split("\\(")[0];
                    caseNameListInSuitFile += lineContent + " ";

                    ClassNameLines.add(i);
                }

                newLines[i] = lines[i];
            }


            int max_line_num = newLines.length;

            if (ClassNameLines.size() >= 1) {
                int i = 0;
                for (i = 0; i < ClassNameLines.size(); i++) {
                    int single_case_line_start = (int) ClassNameLines.get(i);
                    int index = single_case_line_start;
                    int single_case_line_end = max_line_num;
                    while (index < max_line_num - 1) {
                        index += 1;
                        if (lines[index].trim().startsWith("def run_test")) {
                            single_case_line_end = index;
                            break;
                        }
                    }

                    Boolean hasTag = Boolean.FALSE;
                    while (single_case_line_start < single_case_line_end) {
                        single_case_line_start += 1;
                        if (lines[single_case_line_start].trim().replaceAll(" +", "").startsWith("tag='") ||
                                lines[single_case_line_start].trim().replaceAll(" +", "").startsWith("tag=\"") ||
                                lines[single_case_line_start].trim().replaceAll(" +", "").startsWith("tag=['") ||
                                lines[single_case_line_start].trim().replaceAll(" +", "").startsWith("tag=[\"")) {
                            hasTag = Boolean.TRUE;
                            break;
                        }
                    }

                    if (hasTag) {
                        System.out.println("inputTagName");
                        System.out.println(inputTagName);
                        System.out.println("lines[single_case_line_start]:");
                        System.out.println(lines[single_case_line_start]);

                        //  直接替换 lines[single_case_line_start] to  newLines[single_case_line_start]
                        if (lines[single_case_line_start].trim().replaceAll(" +", "").endsWith("\"")) {
                            String inputTagNameTemp = inputTagName;
                            HashSet<String> set = new HashSet<String>();
                            String[] oldTagList = lines[single_case_line_start].replaceAll(" +", "").split("tag=")[1].replaceAll("\"", "").replaceAll(",+", ",").split(",");
                            for (int j = 0; j < oldTagList.length; j++) {
                                if (!set.contains(oldTagList[j])){
                                    set.add(oldTagList[j]);
                                }
                            }

                            String[] inputTagList = inputTagNameTemp.replaceAll(",+", ",").replaceAll(" +", "").replaceAll("'", "").replaceAll("\"", "").split(",");
                            for (int j = 0; j < inputTagList.length; j++) {
                                if (!set.contains(inputTagList[j])){
                                    set.add(inputTagList[j]);
                                }
                            }

                            inputTagNameTemp = "\"";
                            for (String item : set) {
                                inputTagNameTemp += item + ",";
                            }

                            inputTagNameTemp += "\"";
                            inputTagNameTemp = inputTagNameTemp.replace("\",", "\"").replace(",\"", "\"");
                            inputTagNameTemp = inputTagNameTemp.replaceAll("]", "").replaceAll("\\[", "");

                            newLines[single_case_line_start] = "    tag = " + inputTagNameTemp;

                        } else if (lines[single_case_line_start].trim().replaceAll(" +", "").endsWith("'")) {
                            String inputTagNameTemp = inputTagName;
                            HashSet<String> set = new HashSet<String>();
                            String[] oldTagList = lines[single_case_line_start].replaceAll(" +", "").split("tag=")[1].replaceAll("'", "").replaceAll(",+", ",").split(",");
                            for (int j = 0; j < oldTagList.length; j++) {
                                if (!set.contains(oldTagList[j])){
                                    set.add(oldTagList[j]);
                                }
                            }

                            String[] inputTagList = inputTagNameTemp.replaceAll(",+", ",").replaceAll(" +", "").replaceAll("'", "").replaceAll("\"", "").split(",");
                            for (int j = 0; j < inputTagList.length; j++) {
                                if (!set.contains(inputTagList[j])){
                                    set.add(inputTagList[j].replaceAll("]", "").replaceAll("\\[", ""));
                                }
                            }

                            inputTagNameTemp = "'";
                            for (String item : set) {
                                inputTagNameTemp += item + ",";
                            }

                            inputTagNameTemp += "'";
                            inputTagNameTemp = inputTagNameTemp.replace("',", "'").replace(",'", "'").replaceAll("]", "").replaceAll("\\[", "");

                            newLines[single_case_line_start] = "    tag = " + inputTagNameTemp;

                        } else if (lines[single_case_line_start].trim().replaceAll(" +", "").endsWith("\"]")) {
                            String inputTagNameTemp = inputTagName;

                            HashSet<String> set = new HashSet<String>();
                            String[] oldTagList = lines[single_case_line_start].replaceAll(" +", "").split("tag=")[1].replaceAll("'", "").replaceAll("\"", "").replaceAll(",+", ",").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                            for (int j = 0; j < oldTagList.length; j++) {
                                if (!set.contains(oldTagList[j])){
                                    set.add(oldTagList[j]);
                                }
                            }

                            String[] inputTagList = inputTagNameTemp.replaceAll(",+", ",").replaceAll(" +", "").replaceAll("'", "").replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                            for (int j = 0; j < inputTagList.length; j++) {
                                if (!set.contains(inputTagList[j])){
                                    set.add(inputTagList[j]);
                                }
                            }

                            inputTagNameTemp = "[";
                            for (String item : set) {
                                inputTagNameTemp += "\"" + item + "\",";
                            }

                            inputTagNameTemp += "]";
                            inputTagNameTemp = inputTagNameTemp.replace(",]", "]");

                            newLines[single_case_line_start] = "    tag = " + inputTagNameTemp;

                        } else if (lines[single_case_line_start].trim().replaceAll(" +", "").endsWith("\']")) {
                            String inputTagNameTemp = inputTagName;

                            HashSet<String> set = new HashSet<String>();
                            String[] oldTagList = lines[single_case_line_start].replaceAll(" +", "").split("tag=")[1].replaceAll("'", "").replaceAll("\"", "").replaceAll(",+", ",").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                            for (int j = 0; j < oldTagList.length; j++) {
                                if (!set.contains(oldTagList[j])){
                                    set.add(oldTagList[j]);
                                }
                            }

                            String[] inputTagList = inputTagNameTemp.replaceAll(",+", ",").replaceAll(" +", "").replaceAll("'", "").replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                            for (int j = 0; j < inputTagList.length; j++) {
                                if (!set.contains(inputTagList[j])){
                                    set.add(inputTagList[j]);
                                }
                            }

                            inputTagNameTemp = "[";
                            for (String item : set) {
                                inputTagNameTemp += "'" + item + "',";
                            }

                            inputTagNameTemp += "]";
                            inputTagNameTemp = inputTagNameTemp.replace(",]", "]");

                            newLines[single_case_line_start] = "    tag = " + inputTagNameTemp;

                        }

                        newLines[single_case_line_start] = newLines[single_case_line_start].replaceAll(",+", ",");

                    } else {
                        // 没有tag,则增加 tag='xxx' after line single_case_line_start

                        if (System.getProperty("os.name").startsWith("Windows")) {
                            newLines[single_case_line_start - 1] = newLines[single_case_line_start - 1] + "\n" + "    tag = '"+inputTagName+"'";
                        } else {
                            newLines[single_case_line_start - 1] = newLines[single_case_line_start - 1] + System.lineSeparator() + "    tag = '"+inputTagName+"'";
                        }

                    }
                }

            }

            if (" ".equals(caseNameListInSuitFile)) {
                Messages.showMessageDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath(),
                        "该文件中没有找到有效的TestCase:", IconLoader.getIcon("/icons/sdk_16.svg", SdkIcons.class));
                return;
            }

            //  write newLines to case file
            try {
                String filePath = ((EditorImpl) editor).getVirtualFile().getPath();
                Path fP = Path.of(filePath);
                Files.delete(fP);
                Files.createFile(fP);

                if (System.getProperty("os.name").startsWith("Windows")) {
                    for (int i = 0; i < newLines.length; i++) {
                        Files.writeString(fP, newLines[i] + "\n", StandardOpenOption.APPEND);
                    }
                } else {
                    for (int i = 0; i < newLines.length; i++) {
                        Files.writeString(fP, newLines[i] + System.lineSeparator(), StandardOpenOption.APPEND);
                    }
                }

                Messages.showMessageDialog(inputTagName, "设置tag成功", Messages.getInformationIcon());
                project.getBaseDir().refresh(false,true);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        });

        dialogBuilder.show();

    }

}
