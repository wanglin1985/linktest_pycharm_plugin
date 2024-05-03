package com.github.linktest;

import javax.swing.*;

public class TestRunWithInputs {
    private JTextField searchText;
    private JTextField environment;
    private JTextField threadCount;
    private JPanel rootPanel;
    private JRadioButton retryFailedYesRadioButton;
    private JRadioButton retryFailedNoRadioButton;
    private JCheckBox fileCheckBox;
    private JCheckBox consoleCheckBox;
    private JLabel autoScreenshotOnAction;
    private JRadioButton yesAutoScreenshotOnAction;
    private JRadioButton noAutoScreenshotOnAction;
    private JTextPane textPane1;
    private JTextPane textPane2;
    private JTextPane textPane3;


    public JPanel getRootPanel() {
        return rootPanel;
    }

    public String getEnv() {
        return environment.getText();
    }

    public String getInputText() {
        return searchText.getText();
    }

    public void setInputText(String newValue) {
        searchText.setText(newValue);
    }

    public String getThreadCount() {
        return threadCount.getText();
    }

    public JRadioButton getRetryFailedYesRadioButton() {
        return retryFailedYesRadioButton;
    }

    public JRadioButton getRetryFailedNoRadioButton() {
        return retryFailedNoRadioButton;
    }

    public JCheckBox getFileCheckBox() {
        return fileCheckBox;
    }

    public JCheckBox getConsoleCheckBox() {
        return consoleCheckBox;
    }

    public JRadioButton getYesAutoScreenshotOnAction() {
        return yesAutoScreenshotOnAction;
    }
}
