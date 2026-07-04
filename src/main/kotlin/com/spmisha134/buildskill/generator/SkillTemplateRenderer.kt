package com.spmisha134.buildskill.generator

import com.spmisha134.buildskill.generator.template.TemplateRenderer
import com.spmisha134.buildskill.model.skill.SkillDefinition

class SkillTemplateRenderer(
    private val templateRenderer: TemplateRenderer = TemplateRenderer()
) {
    fun render(definition: SkillDefinition): String {
        val normalizedName = SkillNameNormalizer.normalize(definition.name)
        return templateRenderer.render(
            resourcePath = "templates/generated-skill/SKILL.md",
            variables = mapOf(
                "name" to frontMatterValue(normalizedName),
                "description" to frontMatterValue(definition.description),
            )
        )
    }

    private fun frontMatterValue(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }
}
