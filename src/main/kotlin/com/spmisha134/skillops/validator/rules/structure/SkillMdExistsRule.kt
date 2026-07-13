package com.spmisha134.skillops.validator.rules.structure

import com.spmisha134.skillops.validator.SkillRule
import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.validator.rules.common.error
import com.spmisha134.skillops.validator.rules.common.skillMd
import java.nio.file.Files

class SkillMdExistsRule : SkillRule {
    override fun validate(context: SkillValidationContext) =
        if (Files.isRegularFile(skillMd(context.skillDirectoryPath))) {
            emptyList()
        } else {
            listOf(error("SKILL.md is required.", skillMd(context.skillDirectoryPath)))
        }
}
