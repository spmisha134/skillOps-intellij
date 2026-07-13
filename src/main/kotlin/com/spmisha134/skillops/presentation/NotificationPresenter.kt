package com.spmisha134.skillops.presentation

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

object NotificationPresenter {
    fun showInfo(project: Project, message: String) {
        Messages.showInfoMessage(project, message, SKILLOPS_DIALOG_TITLE)
    }

    fun showError(project: Project, message: String) {
        Messages.showErrorDialog(project, message, SKILLOPS_DIALOG_TITLE)
    }
}
