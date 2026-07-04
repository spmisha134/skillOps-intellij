package com.spmisha134.buildskill.model.validation

data class SkillValidationResult(
    val issues: List<SkillValidationIssue>
) {
    val hasErrors: Boolean
        get() = issues.any { it.severity == SkillValidationSeverity.ERROR }

    val hasWarnings: Boolean
        get() = issues.any { it.severity == SkillValidationSeverity.WARNING }
}
