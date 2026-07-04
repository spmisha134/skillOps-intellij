package com.spmisha134.buildskill.model.validation

import java.nio.file.Path

data class SkillValidationContext(
    val projectBasePath: Path,
    val skillDirectoryPath: Path
)
