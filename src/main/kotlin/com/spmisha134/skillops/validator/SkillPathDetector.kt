package com.spmisha134.skillops.validator

import com.spmisha134.skillops.model.validation.SkillValidationPaths
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

class SkillPathDetector {
    fun skillDirectoriesToValidate(
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
                .sorted(compareBy(Path::name))
                .toList()
        }
    }
}
