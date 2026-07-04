package com.spmisha134.buildskill.generator

import com.spmisha134.buildskill.model.validation.SkillValidationPaths
import java.nio.file.Path

class SkillPathResolver {
    fun resolveSkillDirectory(projectBasePath: String, normalizedSkillName: String): Path {
        val safeName = SkillNameNormalizer.normalize(normalizedSkillName)
        return Path.of(projectBasePath)
            .resolve(SkillValidationPaths.AGENTS_DIRECTORY)
            .resolve(SkillValidationPaths.SKILLS_DIRECTORY)
            .resolve(safeName)
            .normalize()
    }
}
