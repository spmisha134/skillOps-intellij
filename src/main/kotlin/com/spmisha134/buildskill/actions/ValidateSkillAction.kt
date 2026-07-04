package com.spmisha134.buildskill.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.spmisha134.buildskill.model.validation.SkillValidationContext
import com.spmisha134.buildskill.model.validation.SkillValidationIssue
import com.spmisha134.buildskill.model.validation.SkillValidationPaths
import com.spmisha134.buildskill.model.validation.SkillValidationResult
import com.spmisha134.buildskill.presentation.NotificationPresenter
import com.spmisha134.buildskill.presentation.ValidationResultPresenter
import com.spmisha134.buildskill.validator.SkillValidator
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

class ValidateSkillAction : AnAction() {

    private val validator = SkillValidator()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project?.basePath != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectRoot = project.projectRoot()
            ?: return project.showValidationError("No project base path is available.")

        val selectedDirectory = event.selectedDirectory() ?: projectRoot
        val skillDirectories = skillDirectoriesToValidate(projectRoot, selectedDirectory)

        if (skillDirectories.isEmpty()) {
            project.showValidationError(
                "Select a skill folder or a project folder containing .agents/skills/."
            )
            return
        }

        val result = validateSkills(projectRoot, skillDirectories)
        ValidationResultPresenter.showValidationResult(project, result)
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

    private fun skillDirectoriesToValidate(
        projectRoot: Path,
        selectedDirectory: Path,
    ): List<Path> {
        val skillsRoot = projectRoot
            .resolve(SkillValidationPaths.AGENTS_DIRECTORY)
            .resolve(SkillValidationPaths.SKILLS_DIRECTORY)
            .normalize()

        val selected = selectedDirectory.normalize()

        return when {
            selected.isSkillDirectoryUnder(skillsRoot) ->
                listOf(selected)

            selected == skillsRoot && Files.isDirectory(skillsRoot) ->
                skillsRoot.childDirectories()

            selected.containsSkillsRoot() ->
                selected.resolve(SkillValidationPaths.AGENTS_DIRECTORY)
                    .resolve(SkillValidationPaths.SKILLS_DIRECTORY)
                    .childDirectories()

            else ->
                emptyList()
        }
    }

    private fun Path.isSkillDirectoryUnder(skillsRoot: Path): Boolean =
        Files.isDirectory(this) && parent == skillsRoot

    private fun Path.containsSkillsRoot(): Boolean =
        Files.isDirectory(resolve(SkillValidationPaths.AGENTS_DIRECTORY).resolve(SkillValidationPaths.SKILLS_DIRECTORY))

    private fun Path.childDirectories(): List<Path> {
        if (!Files.isDirectory(this)) {
            return emptyList()
        }

        return Files.list(this).use { stream ->
            stream
                .filter(Files::isDirectory)
                .toList()
        }
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
