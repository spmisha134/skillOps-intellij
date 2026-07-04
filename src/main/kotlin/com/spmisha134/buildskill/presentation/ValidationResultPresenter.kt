package com.spmisha134.buildskill.presentation

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.spmisha134.buildskill.model.validation.SkillValidationResult
import com.spmisha134.buildskill.model.validation.SkillValidationSeverity

object ValidationResultPresenter {
    fun showValidationResult(project: Project, result: SkillValidationResult) {
        when {
            result.hasErrors -> Messages.showErrorDialog(project, "Codex skill validation found errors:\n${formatIssues(result, SkillValidationSeverity.ERROR)}", "BuildSkill Intellij")
            result.hasWarnings -> Messages.showWarningDialog(project, "Codex skill validation found warnings:\n${formatIssues(result, SkillValidationSeverity.WARNING)}", "BuildSkill Intellij")
            else -> Messages.showInfoMessage(project, "Codex skill validation passed.", "BuildSkill Intellij")
        }
    }

    fun showCreationValidationResult(project: Project, result: SkillValidationResult) {
        when {
            result.hasErrors -> Messages.showErrorDialog(project, "Codex skill created but validation found errors:\n${formatIssues(result, SkillValidationSeverity.ERROR)}", "BuildSkill Intellij")
            result.hasWarnings -> Messages.showWarningDialog(project, "Codex skill created with warnings:\n${formatIssues(result, SkillValidationSeverity.WARNING)}", "BuildSkill Intellij")
            else -> Messages.showInfoMessage(project, "Codex skill created successfully.", "BuildSkill Intellij")
        }
    }

    private fun formatIssues(result: SkillValidationResult, severity: SkillValidationSeverity): String {
        return result.issues
            .filter { it.severity == severity }
            .joinToString(separator = "\n") { "- ${it.message}" }
    }
}
