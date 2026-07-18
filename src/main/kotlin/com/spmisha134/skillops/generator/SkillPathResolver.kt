package com.spmisha134.skillops.generator

import com.spmisha134.skillops.model.skill.SkillPlatform
import java.nio.file.Path

class SkillPathResolver {
    fun resolveSkillDirectory(
        projectBasePath: String,
        normalizedSkillName: String,
        platform: SkillPlatform = SkillPlatform.CODEX,
    ): Path {
        val safeName = SkillNameNormalizer.normalize(normalizedSkillName)
        return Path.of(projectBasePath)
            .resolve(platform.projectDirectory)
            .resolve("skills")
            .resolve(safeName)
            .normalize()
    }
}
