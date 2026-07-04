package com.spmisha134.buildskill.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.spmisha134.buildskill.generator.SkillGenerator
import com.spmisha134.buildskill.model.generation.SkillGenerationRequest
import com.spmisha134.buildskill.model.generation.SkillGenerationResult
import com.spmisha134.buildskill.presentation.CreateSkillExceptionHandler
import com.spmisha134.buildskill.presentation.NotificationPresenter
import com.spmisha134.buildskill.presentation.ValidationResultPresenter
import com.spmisha134.buildskill.ui.CreateSkillDialog
import com.spmisha134.buildskill.model.validation.SkillValidationContext
import com.spmisha134.buildskill.validator.SkillValidator
import java.nio.file.Path

class CreateSkillAction : AnAction() {

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
        skillDefinition: com.spmisha134.buildskill.model.skill.SkillDefinition,
    ) {
        runCatching {
            writeSkill(
                SkillGenerationRequest(
                    projectBasePath = projectRoot.toString(),
                    selectedDirectoryPath = selectedDirectory?.toString() ?: projectRoot.toString(),
                    skillDefinition = skillDefinition,
                )
            )
        }.onSuccess { result ->
            refresh(result.skillDirectoryPath)
            validateCreatedSkill(project, projectRoot, Path.of(result.skillDirectoryPath))
        }.onFailure { error ->
            project.showCreateError(exceptionHandler.userMessage(error))
        }
    }

    private fun writeSkill(request: SkillGenerationRequest): SkillGenerationResult =
        ApplicationManager.getApplication().runWriteAction<SkillGenerationResult> {
            generator.generate(request)
        }

    private fun validateCreatedSkill(project: Project, projectRoot: Path, skillDirectory: Path) {
        val validationResult = validator.validate(
            SkillValidationContext(
                projectBasePath = projectRoot,
                skillDirectoryPath = skillDirectory,
            )
        )

        ValidationResultPresenter.showCreationValidationResult(project, validationResult)
    }

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
