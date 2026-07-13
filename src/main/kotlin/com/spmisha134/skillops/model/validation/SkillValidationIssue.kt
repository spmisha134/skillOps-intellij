package com.spmisha134.skillops.model.validation

data class SkillValidationIssue(
    val severity: SkillValidationSeverity,
    val message: String,
    val filePath: String? = null
)

enum class SkillValidationSeverity {
    ERROR,
    WARNING
}
