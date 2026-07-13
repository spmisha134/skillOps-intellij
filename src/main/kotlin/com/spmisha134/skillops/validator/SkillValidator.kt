package com.spmisha134.skillops.validator

import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.model.validation.SkillValidationResult
import com.spmisha134.skillops.validator.rules.content.ProjectInstructionDiscoveryRule
import com.spmisha134.skillops.validator.rules.content.SmallSkillMdRule
import com.spmisha134.skillops.validator.rules.openai.OpenAiYamlRule
import com.spmisha134.skillops.validator.rules.references.RequiredReferencesRule
import com.spmisha134.skillops.validator.rules.structure.FrontMatterRule
import com.spmisha134.skillops.validator.rules.structure.RequiredFieldsRule
import com.spmisha134.skillops.validator.rules.structure.SafeFolderNameRule
import com.spmisha134.skillops.validator.rules.structure.SkillLocationRule
import com.spmisha134.skillops.validator.rules.structure.SkillMdExistsRule

class SkillValidator(
    private val rules: List<SkillRule> = defaultRules()
) {
    fun validate(context: SkillValidationContext): SkillValidationResult {
        return SkillValidationResult(
            issues = rules.flatMap { it.validate(context) }
        )
    }

    companion object {
        fun defaultRules(): List<SkillRule> = listOf(
            SkillLocationRule(),
            SkillMdExistsRule(),
            FrontMatterRule(),
            RequiredFieldsRule(),
            SmallSkillMdRule(),
            RequiredReferencesRule(),
            ProjectInstructionDiscoveryRule(),
            SafeFolderNameRule(),
            OpenAiYamlRule()
        )
    }
}
