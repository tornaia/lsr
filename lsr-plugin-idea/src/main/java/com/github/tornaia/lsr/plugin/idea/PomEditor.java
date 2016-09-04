package com.github.tornaia.lsr.plugin.idea;

import com.intellij.openapi.editor.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.Optional;

public class PomEditor {

    private Editor editor;

    public PomEditor(Editor editor) {
        this.editor = editor;
    }

    public Optional<Dependency> getSelectedDependency() {
        Document pomDocument = editor.getDocument();
        String fileContent = pomDocument.getText();
        CaretModel caretModel = editor.getCaretModel();
        Caret currentCaret = caretModel.getCurrentCaret();
        VisualPosition start = currentCaret.getSelectionStartPosition();
        VisualPosition end = currentCaret.getSelectionEndPosition();

        int startLine = start.getLine();
        int endLine = end.getLine();
        return getSelectedDependency(fileContent, startLine, endLine);
    }

    private static Optional<Dependency> getSelectedDependency(String fileContent, int lineStart, int lineEnd) {
        int subStringStartPos = StringUtils.ordinalIndexOf(fileContent, "\n", lineStart);
        subStringStartPos = subStringStartPos == -1 ? 0 : subStringStartPos;
        int subStringEndPos = StringUtils.ordinalIndexOf(fileContent, "\n", lineEnd + 1);
        while (true) {
            String substring = fileContent.substring(subStringStartPos, subStringEndPos);
            boolean startsWithDependencyTag = substring.startsWith("<dependency>");
            boolean endsWithDependencyTag = substring.endsWith("</dependency>");
            if (startsWithDependencyTag && endsWithDependencyTag) {
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(Model.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    StringReader reader = new StringReader(substring);
                    JAXBElement<Dependency> root = unmarshaller.unmarshal(new StreamSource(reader), Dependency.class);
                    Dependency dependency = root.getValue();
                    return Optional.of(dependency);
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
            }
            boolean startPosModified = false;
            if (!startsWithDependencyTag) {
                if (subStringStartPos > 0) {
                    subStringStartPos--;
                    startPosModified = true;
                }
            }

            boolean endPosModified = false;
            if (!endsWithDependencyTag) {
                if (subStringEndPos < fileContent.length()) {
                    subStringEndPos++;
                    endPosModified = true;
                }
            }

            if (!startPosModified && !endPosModified) {
                return Optional.empty();
            }
        }
    }
}
