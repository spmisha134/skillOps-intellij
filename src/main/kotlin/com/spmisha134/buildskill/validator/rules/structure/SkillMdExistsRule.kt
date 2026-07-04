package com.spmisha134.buildskill.validator.rules.structure

import com.spmisha134.buildskill.validator.SkillRule
import com.spmisha134.buildskill.model.validation.SkillValidationContext
import com.spmisha134.buildskill.validator.rules.common.error
import com.spmisha134.buildskill.validator.rules.common.skillMd
import java.nio.file.Files

class SkillMdExistsRule : SkillRule {
    override fun validate(context: SkillValidationContext) =
        if (Files.isRegularFile(skillMd(context.skillDirectoryPath))) {
            emptyList()
        } else {
            listOf(error("SKILL.md is required.", skillMd(context.skillDirectoryPath)))
        }
}
