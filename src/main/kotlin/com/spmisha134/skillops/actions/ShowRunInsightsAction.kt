package com.spmisha134.skillops.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.spmisha134.skillops.insights.presentation.RunInsightsReportFormatter
import com.spmisha134.skillops.insights.run.SkillOpsRunInsightsService
import com.spmisha134.skillops.insights.settings.SkillOpsInsightsSettingsState
import com.spmisha134.skillops.insights.ui.RunInsightsDialog
import com.spmisha134.skillops.presentation.NotificationPresenter
import java.nio.file.Path

class ShowRunInsightsAction : AnAction() {
    private val insightsService = SkillOpsRunInsightsService()
    private val formatter = RunInsightsReportFormatter()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project?.basePath != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectRoot = project.basePath?.let(Path::of)
            ?: return NotificationPresenter.showError(project, "No project base path is available.")

        runCatching {
            insightsService.buildReport(
                projectRoot = projectRoot,
                settings = SkillOpsInsightsSettingsState.getInstance().settings,
            )
        }.onSuccess { report ->
            RunInsightsDialog(project, formatter.format(report)).show()
        }.onFailure { error ->
            NotificationPresenter.showError(
                project,
                "Could not show SkillOps run insights: ${error.message ?: error.javaClass.simpleName}"
            )
        }
    }
}
