package com.spmisha134.skillops.model.generation

import com.spmisha134.skillops.model.skill.SkillDefinition

data class SkillGenerationRequest(
    val projectBasePath: String,
    val selectedDirectoryPath: String,
    val skillDefinition: SkillDefinition
)
