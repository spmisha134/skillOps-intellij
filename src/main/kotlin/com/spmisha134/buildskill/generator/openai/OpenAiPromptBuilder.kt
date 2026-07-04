package com.spmisha134.buildskill.generator.openai

import com.spmisha134.buildskill.generator.SkillNameNormalizer
import com.spmisha134.buildskill.model.skill.SkillDefinition

class OpenAiPromptBuilder {
    fun defaultPrompt(definition: SkillDefinition): String {
        val normalizedName = SkillNameNormalizer.normalize(definition.name)
        return "Use \$${normalizedName} to ${definition.description.toPromptFragment()}. " +
            "Follow the workflow in references/instructions.md, " +
            "validate using references/validation.md, and use references/examples.md when examples are needed."
    }

    private fun String.toPromptFragment(): String =
        trim()
            .trimEnd('.')
            .replaceFirstChar { it.lowercase() }
}
