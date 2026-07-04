package com.spmisha134.buildskill.validator.rules.structure

import com.spmisha134.buildskill.model.validation.SkillValidationPaths
import com.spmisha134.buildskill.validator.SkillRule
import com.spmisha134.buildskill.model.validation.SkillValidationContext
import com.spmisha134.buildskill.validator.rules.common.error

class SkillLocationRule : SkillRule {
    override fun validate(context: SkillValidationContext) =
        if (context.skillDirectoryPath.normalize().parent == context.projectBasePath.resolve(SkillValidationPaths.AGENTS_DIRECTORY).resolve(SkillValidationPaths.SKILLS_DIRECTORY).normalize()) {
            emptyList()
        } else {
            listOf(error("Skill folder must be under .agents/skills/.", context.skillDirectoryPath))
        }
}
