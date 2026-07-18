package com.spmisha134.skillops.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.spmisha134.skillops.insights.presentation.RunInsightsReportFormatter
import com.spmisha134.skillops.insights.run.SkillOpsRunInsightsService
import com.spmisha134.skillops.insights.run.SkillOpsRunInsightsReport
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
        val settings = SkillOpsInsightsSettingsState.getInstance().settings.copy()

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Loading Codex run insights", false) {
                private lateinit var report: SkillOpsRunInsightsReport

                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    indicator.text = "Scanning Codex sessions…"
                    report = insightsService.buildReport(
                        projectRoot = projectRoot,
                        settings = settings,
                    )
                }

                override fun onSuccess() {
                    RunInsightsDialog(project, report, formatter).show()
                }

                override fun onThrowable(error: Throwable) {
                    showLoadError(project, error)
                }
            }
        )
    }

    private fun showLoadError(project: Project, error: Throwable) {
        NotificationPresenter.showError(
            project,
            "Could not show SkillOps run insights: ${error.message ?: error.javaClass.simpleName}"
        )
    }
}
