package com.spmisha134.skillops.validator.rules.structure

import com.spmisha134.skillops.validator.SkillRule
import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.validator.rules.common.error
import com.spmisha134.skillops.validator.rules.common.frontMatter
import com.spmisha134.skillops.validator.rules.common.readSkillMd
import com.spmisha134.skillops.validator.rules.common.skillMd

class FrontMatterRule : SkillRule {
    override fun validate(context: SkillValidationContext): List<com.spmisha134.skillops.model.validation.SkillValidationIssue> {
        val content = readSkillMd(context.skillDirectoryPath) ?: return emptyList()
        return if (frontMatter(content) != null) {
            emptyList()
        } else {
            listOf(error("SKILL.md must start with YAML front matter.", skillMd(context.skillDirectoryPath)))
        }
    }
}
