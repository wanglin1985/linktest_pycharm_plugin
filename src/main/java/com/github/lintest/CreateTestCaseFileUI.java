package com.github.lintest;

import javax.swing.*;

public class CreateTestCaseFileUI {
    private JPanel rootPanel;
    private JCheckBox apiCheckBox;
    private JCheckBox uiCheckBox;
    private JCheckBox androidCheckBox;
    private JCheckBox iosCheckBox;

    private JTextField fileName;
    private JTextField packagePath;
    private JRadioButton yesRadioButton;
    private JRadioButton noRadioButton;


    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JRadioButton getYesRadioButton() {
        return yesRadioButton;
    }

    public JRadioButton getNoRadioButton() {
        return noRadioButton;
    }

    public void setPackagePath(String newValue) {
        packagePath.setText(newValue);
    }

    public String getFileName() {
        return fileName.getText();
    }

    public void setFileName(String newValue) {
        fileName.setText(newValue);
    }

    public JTextField getPackagePath() {
        return packagePath;
    }


    public JCheckBox getApiCheckBox() {
        return apiCheckBox;
    }

    public JCheckBox getUiCheckBox() {
        return uiCheckBox;
    }

    public JCheckBox getAndroidCheckBox() {
        return androidCheckBox;
    }

    public JCheckBox getIosCheckBox() {
        return iosCheckBox;
    }


}
