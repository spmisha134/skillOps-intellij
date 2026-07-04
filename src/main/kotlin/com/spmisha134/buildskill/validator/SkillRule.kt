package com.spmisha134.buildskill.validator

import com.spmisha134.buildskill.model.validation.SkillValidationIssue
import com.spmisha134.buildskill.model.validation.SkillValidationContext

interface SkillRule {
    fun validate(context: SkillValidationContext): List<SkillValidationIssue>
}
