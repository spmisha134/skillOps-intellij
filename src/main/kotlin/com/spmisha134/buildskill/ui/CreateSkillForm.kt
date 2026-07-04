package com.spmisha134.buildskill.ui

import com.intellij.ui.components.JBTextField
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class CreateSkillForm {
    val skillNameField = JBTextField()
    val descriptionField = JBTextField()
    val createScriptsDirectoryCheckBox = JCheckBox("Create scripts folder", true)
    val createAssetsDirectoryCheckBox = JCheckBox("Create assets folder", true)

    val panel: JComponent = JPanel(GridBagLayout()).apply {
        var row = 0
        addLabeled("Skill name", skillNameField, row++)
        addLabeled("Description", descriptionField, row++)
        addFullWidth(createScriptsDirectoryCheckBox, row++)
        addFullWidth(createAssetsDirectoryCheckBox, row)
    }

    private fun JPanel.addLabeled(label: String, component: JComponent, row: Int) {
        add(
            JLabel(label),
            GridBagConstraints().apply {
                gridx = 0
                gridy = row
                anchor = GridBagConstraints.NORTHWEST
                insets.set(4, 0, 4, 8)
            }
        )
        add(
            component,
            GridBagConstraints().apply {
                gridx = 1
                gridy = row
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                insets.set(4, 0, 4, 0)
            }
        )
    }

    private fun JPanel.addFullWidth(component: JComponent, row: Int) {
        add(
            component,
            GridBagConstraints().apply {
                gridx = 1
                gridy = row
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                insets.set(2, 0, 2, 0)
            }
        )
    }
}
