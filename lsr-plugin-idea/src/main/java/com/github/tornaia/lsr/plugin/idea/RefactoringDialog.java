package com.github.tornaia.lsr.plugin.idea;

import com.github.tornaia.lsr.action.MoveDependencyToAnotherModuleAction;
import com.github.tornaia.lsr.action.MoveJavaSourcesToAnAnotherModuleAction;
import com.github.tornaia.lsr.action.WriteToDiskAction;
import com.github.tornaia.lsr.model.MavenCoordinates;
import com.github.tornaia.lsr.util.ParentChildMapUtils;
import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RefactoringDialog extends JDialog {

    private static final MavenCoordinates NEW_MAVEN_MODULE_COORDINATE = new MavenCoordinates("NEW", "NEW", "NEW");

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField whatTextField;
    private JComboBox targetComboBox;
    private JTextField newModuleMavenCoordinate;
    private JComboBox newModulesParentMavenCoordinate;
    private JLabel newModuleMavenCoordinateLabel;
    private JLabel newModulesParentMavenCoordinateLabel;
    private MavenCoordinates what;
    private MavenCoordinates from;
    private File topLevelPom;

    public RefactoringDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        targetComboBox.addActionListener(e -> targetChanged());

        moveToCenter();
    }

    private void moveToCenter() {
        setLocationRelativeTo(null);
    }

    private void targetChanged() {
        boolean isNewSelected = Objects.equals(NEW_MAVEN_MODULE_COORDINATE, targetComboBox.getSelectedItem());
        newModuleMavenCoordinate.setVisible(isNewSelected);
        newModuleMavenCoordinateLabel.setVisible(isNewSelected);
        newModulesParentMavenCoordinate.setVisible(isNewSelected);
        newModulesParentMavenCoordinateLabel.setVisible(isNewSelected);
    }

    private void onOK() {
        Multimap<Model, Model> parentChildMap = ParseUtils.explore(topLevelPom);

        boolean isNewSelected = Objects.equals(NEW_MAVEN_MODULE_COORDINATE, targetComboBox.getSelectedItem());
        MavenCoordinates as;
        MavenCoordinates parentTo;
        if (isNewSelected) {
            String mavenCoordinateText = newModuleMavenCoordinate.getText();
            String[] rawMavenCoordinate = mavenCoordinateText.split(":");
            as = new MavenCoordinates(rawMavenCoordinate[0], rawMavenCoordinate[1], (rawMavenCoordinate[2]));
            parentTo = (MavenCoordinates) newModulesParentMavenCoordinate.getSelectedItem();
        } else {
            as = (MavenCoordinates) targetComboBox.getSelectedItem();
            parentTo = ParentChildMapUtils.getParentTo(parentChildMap, as);
        }


        File rootDirectory = topLevelPom.getParentFile();

        new MoveDependencyToAnotherModuleAction(parentChildMap, from, as, parentTo, what).execute();
        new WriteToDiskAction(topLevelPom, parentChildMap).execute();
        new MoveJavaSourcesToAnAnotherModuleAction(rootDirectory, from, as, what).execute();

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public void setWhat(Dependency what) {
        this.what = new MavenCoordinates(what.getGroupId(), what.getArtifactId(), what.getVersion());
        whatTextField.setText(what.getGroupId() + ":" + what.getArtifactId() + ":" + what.getVersion());
    }

    public void setFrom(MavenCoordinates from) {
        this.from = from;
    }

    public void setTopLevelPom(File topLevelPom) {
        this.topLevelPom = topLevelPom;
    }

    public void setTargets(List<Model> targets) {
        List<MavenCoordinates> allModules = targets.stream()
                .map(model -> new MavenCoordinates(model.getGroupId(), model.getArtifactId(), model.getVersion()))
                .collect(Collectors.toList());

        List<MavenCoordinates> targetMavenCoordinates = Lists.newArrayList(allModules);
        targetMavenCoordinates.add(NEW_MAVEN_MODULE_COORDINATE);
        targetMavenCoordinates.remove(what);

        targetComboBox.setModel(new CollectionComboBoxModel(targetMavenCoordinates));
        newModulesParentMavenCoordinate.setModel(new CollectionComboBoxModel(allModules));
        targetChanged();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(100, 100));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(600, 300), null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("What");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        whatTextField = new JTextField();
        panel3.add(whatTextField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        targetComboBox = new JComboBox();
        panel3.add(targetComboBox, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Target");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        newModuleMavenCoordinateLabel = new JLabel();
        newModuleMavenCoordinateLabel.setText("NewModuleMavenCoordinate");
        panel3.add(newModuleMavenCoordinateLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        newModulesParentMavenCoordinateLabel = new JLabel();
        newModulesParentMavenCoordinateLabel.setText("NewModulesParentMavenCoordindate");
        panel3.add(newModulesParentMavenCoordinateLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("MoveToExistingModule");
        panel3.add(label3, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        newModuleMavenCoordinate = new JTextField();
        panel3.add(newModuleMavenCoordinate, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        newModulesParentMavenCoordinate = new JComboBox();
        panel3.add(newModulesParentMavenCoordinate, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
