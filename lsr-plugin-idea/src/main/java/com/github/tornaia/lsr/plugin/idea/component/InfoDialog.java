package com.github.tornaia.lsr.plugin.idea.component;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class InfoDialog extends DialogWrapper {

    private String text;

    public InfoDialog(String title, String text) {
        super(false);
        setTitle(title);
        this.text = text;
        init();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{this.getOKAction()};
    }

    protected JComponent createCenterPanel() {
        JPanel jPanel = new JPanel();
        JLabel jLabel = new JLabel(text);
        jPanel.add(jLabel);
        return jPanel;
    }
}
