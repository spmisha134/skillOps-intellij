package com.spmisha134.skillops.model.generation

import com.spmisha134.skillops.model.skill.SkillDefinition
import com.spmisha134.skillops.model.skill.SkillPlatform

data class SkillGenerationRequest(
    val projectBasePath: String,
    val selectedDirectoryPath: String,
    val skillDefinition: SkillDefinition,
    val platform: SkillPlatform,
) {
    constructor(
        projectBasePath: String,
        selectedDirectoryPath: String,
        skillDefinition: SkillDefinition,
    ) : this(projectBasePath, selectedDirectoryPath, skillDefinition, SkillPlatform.CODEX)
}
