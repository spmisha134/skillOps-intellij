package com.spmisha134.buildskill.validator.rules.structure

import com.spmisha134.buildskill.generator.SkillNameNormalizer
import com.spmisha134.buildskill.validator.SkillRule
import com.spmisha134.buildskill.model.validation.SkillValidationContext
import com.spmisha134.buildskill.validator.rules.common.error

class SafeFolderNameRule : SkillRule {
    override fun validate(context: SkillValidationContext): List<com.spmisha134.buildskill.model.validation.SkillValidationIssue> {
        val folderName = context.skillDirectoryPath.fileName?.toString().orEmpty()
        val normalized = runCatching { SkillNameNormalizer.normalize(folderName) }.getOrNull()

        return if (folderName == normalized) {
            emptyList()
        } else {
            listOf(error("Skill folder name must be normalized and safe.", context.skillDirectoryPath))
        }
    }
}
