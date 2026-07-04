package com.spmisha134.buildskill.validator.rules.content

import com.spmisha134.buildskill.validator.SkillRule
import com.spmisha134.buildskill.model.validation.SkillValidationContext
import com.spmisha134.buildskill.validator.rules.common.readSkillMd
import com.spmisha134.buildskill.validator.rules.common.skillMd
import com.spmisha134.buildskill.validator.rules.common.warning

class SmallSkillMdRule(
    private val maxCharacters: Int = 1200
) : SkillRule {
    override fun validate(context: SkillValidationContext): List<com.spmisha134.buildskill.model.validation.SkillValidationIssue> {
        val content = readSkillMd(context.skillDirectoryPath) ?: return emptyList()
        return if (content.length > maxCharacters) {
            listOf(warning("SKILL.md is larger than expected.", skillMd(context.skillDirectoryPath)))
        } else {
            emptyList()
        }
    }
}
