package com.spmisha134.buildskill.model.generation

data class SkillGenerationResult(
    val skillDirectoryPath: String,
    val skillMdPath: String,
    val createdFiles: List<String>,
    val createdDirectories: List<String>
)
