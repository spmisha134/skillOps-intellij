package com.spmisha134.buildskill.model.generation

import com.spmisha134.buildskill.model.skill.SkillDefinition

data class SkillGenerationRequest(
    val projectBasePath: String,
    val selectedDirectoryPath: String,
    val skillDefinition: SkillDefinition
)
