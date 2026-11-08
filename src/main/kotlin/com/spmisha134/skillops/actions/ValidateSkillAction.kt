package com.spmisha134.skillops.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.model.validation.SkillValidationIssue
import com.spmisha134.skillops.model.validation.SkillValidationResult
import com.spmisha134.skillops.presentation.NotificationPresenter
import com.spmisha134.skillops.presentation.ValidationResultPresenter
import com.spmisha134.skillops.validator.SkillPathDetector
import com.spmisha134.skillops.validator.SkillValidator
import java.nio.file.Path
import kotlin.io.path.name

class ValidateSkillAction : AnAction() {

    private val validator = SkillValidator()
    private val pathDetector = SkillPathDetector()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project?.basePath != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectRoot = project.projectRoot()
            ?: return project.showValidationError("No project base path is available.")

        val selectedDirectory = event.selectedDirectory() ?: projectRoot
        ProgressManager.getInstance().run(
            object : Task.Modal(project, "Validating SkillOps Skills", true) {
                private var skillDirectories: List<Path> = emptyList()
                private lateinit var result: SkillValidationResult

                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    indicator.text = "Discovering skills…"
                    indicator.checkCanceled()
                    skillDirectories = pathDetector.skillDirectoriesToValidate(projectRoot, selectedDirectory)
                    if (skillDirectories.isEmpty()) return
                    indicator.text = "Validating ${skillDirectories.size} skill${if (skillDirectories.size == 1) "" else "s"}…"
                    result = validateSkills(projectRoot, skillDirectories)
                    indicator.checkCanceled()
                }

                override fun onSuccess() {
                    if (skillDirectories.isEmpty()) {
                        project.showValidationError(
                            "Select a skill folder or a project folder containing .agents/skills/."
                        )
                    } else {
                        ValidationResultPresenter.showValidationResult(project, result)
                    }
                }

                override fun onThrowable(error: Throwable) {
                    project.showValidationError(error.message ?: error.javaClass.simpleName)
                }
            }
        )
    }

    private fun validateSkills(
        projectRoot: Path,
        skillDirectories: List<Path>,
    ): SkillValidationResult {
        val issues = skillDirectories.flatMap { skillDirectory ->
            validator.validate(SkillValidationContext(projectRoot, skillDirectory))
                .issues
                .map { issue -> issue.withSkillPrefix(skillDirectory.name) }
        }

        return SkillValidationResult(issues)
    }

    private fun AnActionEvent.selectedDirectory(): Path? =
        getData(CommonDataKeys.VIRTUAL_FILE)
            ?.let { selected -> if (selected.isDirectory) selected else selected.parent }
            ?.path
            ?.let(Path::of)

    private fun Project.projectRoot(): Path? =
        basePath?.let(Path::of)

    private fun Project.showValidationError(message: String) {
        NotificationPresenter.showError(this, "Could not validate skill: $message")
    }

    private fun SkillValidationIssue.withSkillPrefix(skillName: String): SkillValidationIssue =
        copy(message = "[$skillName] $message")
}
