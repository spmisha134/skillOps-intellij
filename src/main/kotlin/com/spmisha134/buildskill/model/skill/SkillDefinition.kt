package com.spmisha134.buildskill.model.skill

data class SkillDefinition(
    val name: String,
    val description: String,
    val createScriptsDirectory: Boolean = true,
    val createAssetsDirectory: Boolean = true,
)
