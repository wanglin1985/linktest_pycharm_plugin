package com.github.linktest;

import javax.swing.*;

public class TestRunWithInputs {
    private JTextField searchText;
    private JTextField environment;
    private JTextField threadCount;
    private JPanel rootPanel;


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
}
