package com.spmisha134.skillops.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.spmisha134.skillops.generator.SkillGenerator
import com.spmisha134.skillops.model.generation.SkillGenerationRequest
import com.spmisha134.skillops.model.skill.SkillPlatform
import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.model.validation.SkillValidationResult
import com.spmisha134.skillops.presentation.CreateSkillExceptionHandler
import com.spmisha134.skillops.presentation.NotificationPresenter
import com.spmisha134.skillops.presentation.ValidationResultPresenter
import com.spmisha134.skillops.ui.CreateSkillDialog
import com.spmisha134.skillops.validator.SkillValidator
import java.nio.file.Path

open class CreateSkillAction(
    private val platform: SkillPlatform = SkillPlatform.CODEX,
) : AnAction() {

    private val generator = SkillGenerator()
    private val validator = SkillValidator()
    private val exceptionHandler = CreateSkillExceptionHandler()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project?.basePath != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectRoot = project.basePath?.let(Path::of)
            ?: return project.showCreateError("No project base path is available.")

        val skillDefinition = CreateSkillDialog(project)
            .takeIf { it.showAndGet() }
            ?.skillDefinition()
            ?: return

        createSkill(project, projectRoot, selectedDirectory(event), skillDefinition)
    }

    private fun createSkill(
        project: Project,
        projectRoot: Path,
        selectedDirectory: Path?,
        skillDefinition: com.spmisha134.skillops.model.skill.SkillDefinition,
    ) {
        val request = SkillGenerationRequest(
            projectBasePath = projectRoot.toString(),
            selectedDirectoryPath = selectedDirectory?.toString() ?: projectRoot.toString(),
            skillDefinition = skillDefinition,
            platform = platform,
        )
        ProgressManager.getInstance().run(
            object : Task.Modal(project, "Creating ${platform.displayName} skill", true) {
                private lateinit var outcome: CreateSkillOutcome

                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    indicator.text = "Writing skill files…"
                    indicator.checkCanceled()
                    val result = generator.generate(request)
                    refresh(result.skillDirectoryPath)
                    indicator.checkCanceled()
                    val validation = if (platform == SkillPlatform.CODEX) {
                        indicator.text = "Validating generated skill…"
                        validator.validate(
                            SkillValidationContext(
                                projectBasePath = projectRoot,
                                skillDirectoryPath = Path.of(result.skillDirectoryPath),
                            )
                        )
                    } else {
                        null
                    }
                    outcome = CreateSkillOutcome(validation)
                }

                override fun onSuccess() {
                    val validation = outcome.validation
                    if (validation != null) {
                        ValidationResultPresenter.showCreationValidationResult(project, validation)
                    } else {
                        NotificationPresenter.showInfo(project, "${platform.displayName} skill created successfully.")
                    }
                }

                override fun onThrowable(error: Throwable) {
                    project.showCreateError(exceptionHandler.userMessage(error))
                }
            }
        )
    }

    private data class CreateSkillOutcome(
        val validation: SkillValidationResult?,
    )

    private fun selectedDirectory(event: AnActionEvent): Path? =
        event.getData(CommonDataKeys.VIRTUAL_FILE)
            ?.let { selected -> if (selected.isDirectory) selected else selected.parent }
            ?.path
            ?.let(Path::of)

    private fun refresh(path: String) {
        LocalFileSystem.getInstance()
            .refreshAndFindFileByNioFile(Path.of(path))
            ?.refresh(false, true)
    }

    private fun Project.showCreateError(message: String) {
        NotificationPresenter.showError(this, "Could not create skill: $message")
    }
}

class CreateClaudeSkillAction : CreateSkillAction(SkillPlatform.CLAUDE)

class CreateGeminiSkillAction : CreateSkillAction(SkillPlatform.GEMINI)
