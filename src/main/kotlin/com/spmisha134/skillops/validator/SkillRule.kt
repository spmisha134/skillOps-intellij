package com.spmisha134.skillops.validator

import com.spmisha134.skillops.model.validation.SkillValidationIssue
import com.spmisha134.skillops.model.validation.SkillValidationContext

interface SkillRule {
    fun validate(context: SkillValidationContext): List<SkillValidationIssue>
}
