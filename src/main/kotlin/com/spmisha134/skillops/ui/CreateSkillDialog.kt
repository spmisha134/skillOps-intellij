package com.spmisha134.skillops.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.spmisha134.skillops.generator.SkillNameNormalizer
import com.spmisha134.skillops.model.skill.SkillDefinition
import javax.swing.JComponent

class CreateSkillDialog(project: Project) : DialogWrapper(project) {
    private val form = CreateSkillForm()

    init {
        title = "New SkillOps"
        init()
    }

    override fun createCenterPanel(): JComponent = form.panel

    override fun doValidate(): ValidationInfo? {
        return when {
            form.skillNameField.text.isBlank() -> ValidationInfo("Skill name is required.", form.skillNameField)
            form.descriptionField.text.isBlank() -> ValidationInfo("Description is required.", form.descriptionField)
            else -> runCatching {
                SkillNameNormalizer.normalize(form.skillNameField.text)
                null
            }.getOrElse { ValidationInfo(it.message ?: "Skill name is invalid.", form.skillNameField) }
        }
    }

    fun skillDefinition(): SkillDefinition {
        return SkillDefinition(
            name = form.skillNameField.text.trim(),
            description = form.descriptionField.text.trim(),
            createScriptsDirectory = form.createScriptsDirectoryCheckBox.isSelected,
            createAssetsDirectory = form.createAssetsDirectoryCheckBox.isSelected,
        )
    }
}
