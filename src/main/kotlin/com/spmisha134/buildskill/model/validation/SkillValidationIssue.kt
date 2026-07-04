package com.spmisha134.buildskill.model.validation

data class SkillValidationIssue(
    val severity: SkillValidationSeverity,
    val message: String,
    val filePath: String? = null
)

enum class SkillValidationSeverity {
    ERROR,
    WARNING
}
