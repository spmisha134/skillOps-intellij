package com.spmisha134.buildskill.validator.rules.structure

import com.spmisha134.buildskill.model.validation.FrontMatterRequirement
import com.spmisha134.buildskill.validator.SkillRule
import com.spmisha134.buildskill.model.validation.SkillValidationContext
import com.spmisha134.buildskill.model.validation.SkillValidationIssue
import com.spmisha134.buildskill.validator.rules.common.error
import com.spmisha134.buildskill.validator.rules.common.frontMatter
import com.spmisha134.buildskill.validator.rules.common.readSkillMd
import com.spmisha134.buildskill.validator.rules.common.skillMd

class RequiredFieldsRule : SkillRule {
    private val requiredFields = listOf(
        FrontMatterRequirement("name", "SKILL.md front matter must include name."),
        FrontMatterRequirement("description", "SKILL.md front matter must include a non-blank description."),
    )

    override fun validate(context: SkillValidationContext): List<SkillValidationIssue> {
        val content = readSkillMd(context.skillDirectoryPath) ?: return emptyList()
        val frontMatter = frontMatter(content) ?: return emptyList()
        val path = skillMd(context.skillDirectoryPath)

        return requiredFields.mapNotNull { requirement ->
            requirement
                .takeIf { frontMatter[it.key].isNullOrBlank() }
                ?.let { error(it.missingMessage, path) }
        }
    }
}
