package com.github.lintest;

import javax.swing.*;

public class SetTagsUI {
    private JTextField tagName;
    private JPanel rootPanel;

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public String getTagName() {
        return tagName.getText();
    }

    public void setTagName(String newValue) {
        tagName.setText(newValue);
    }
}
