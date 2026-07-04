package com.spmisha134.buildskill.generator

import com.spmisha134.buildskill.generator.template.TemplateRenderer
import com.spmisha134.buildskill.model.skill.SkillDefinition

class ReferenceTemplateRenderer(
    private val templateRenderer: TemplateRenderer = TemplateRenderer()
) {
    fun renderInstructions(definition: SkillDefinition): String {
        return templateRenderer.render("templates/generated-skill/references/instructions.md")
    }

    fun renderValidation(definition: SkillDefinition): String {
        return templateRenderer.render("templates/generated-skill/references/validation.md")
    }

    fun renderExamples(definition: SkillDefinition): String {
        return templateRenderer.render("templates/generated-skill/references/examples.md")
    }
}
