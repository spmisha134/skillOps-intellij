package com.spmisha134.buildskill.presentation

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

object NotificationPresenter {
    fun showInfo(project: Project, message: String) {
        Messages.showInfoMessage(project, message, "BuildSkill Intellij")
    }

    fun showError(project: Project, message: String) {
        Messages.showErrorDialog(project, message, "BuildSkill Intellij")
    }
}
