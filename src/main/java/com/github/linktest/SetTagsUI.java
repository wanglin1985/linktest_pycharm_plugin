package com.github.linktest;

import javax.swing.*;

public class SetTagsUI {
    private JTextField tagName;
    private JPanel rootPanel;
    private JList list1;

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
