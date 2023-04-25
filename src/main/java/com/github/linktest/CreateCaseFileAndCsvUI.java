package com.github.linktest;

import javax.swing.*;

public class CreateCaseFileAndCsvUI {
    private JPanel rootPanel;
    private JTextField fileName;
    private JTextField packagePath;


    public JPanel getRootPanel() {
        return rootPanel;
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

    public void setPackagePath(String newValue) {
        packagePath.setText(newValue);
    }


}
