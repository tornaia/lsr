package com.github.tornaia.lsr.plugin.idea;

import com.github.tornaia.lsr.util.ParseUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.apache.maven.model.Dependency;

import java.util.Optional;

public class RefactoringMenuAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Document currentDoc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
        String fileContent = currentDoc.getText();

        Caret currentCaret = FileEditorManager.getInstance(project).getSelectedTextEditor().getCaretModel().getCurrentCaret();
        VisualPosition start = currentCaret.getSelectionStartPosition();
        VisualPosition end = currentCaret.getSelectionEndPosition();

        Optional<Dependency> selected = ParseUtils.getSelectedDependency(fileContent, start.getLine(), end.getLine());

        selected.ifPresent(dependency -> showDialog(dependency));
    }

    private void showDialog(Dependency what) {
        RefactoringDialog dialog = new RefactoringDialog();
        dialog.setWhat(what);
        dialog.pack();
        dialog.setVisible(true);
    }
}