package com.spmisha134.skillops.generator.openai

import com.spmisha134.skillops.model.skill.SkillDefinition
import com.spmisha134.skillops.generator.template.TemplateRenderer

class OpenAiYamlRenderer(
    private val templateRenderer: TemplateRenderer = TemplateRenderer(),
    private val promptBuilder: OpenAiPromptBuilder = OpenAiPromptBuilder(),
) {
    fun render(definition: SkillDefinition): String {
        return templateRenderer.render(
            resourcePath = "templates/generated-skill/agents/openai.yaml",
            variables = mapOf(
                "display_name" to YamlScalarFormatter.quoted(definition.name.trim()),
                "short_description" to YamlScalarFormatter.quoted(definition.description.trim()),
                "default_prompt" to YamlScalarFormatter.quoted(promptBuilder.defaultPrompt(definition)),
            )
        )
    }
}
