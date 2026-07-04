package com.spmisha134.buildskill.generator.openai

import com.spmisha134.buildskill.generator.testDefinition
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiYamlRendererTest {
    @Test
    fun `renders openai interface content`() {
        val content = OpenAiYamlRenderer().render(
            testDefinition(
                name = "Review Notes",
                description = "Summarize repository review notes",
            )
        )

        assertTrue(content.contains("interface:"))
        assertTrue(content.contains("display_name: \"Review Notes\""))
        assertTrue(content.contains("short_description: \"Summarize repository review notes\""))
        assertTrue(
            content.contains(
                "default_prompt: \"Use \$review-notes to summarize repository review notes. " +
                    "Follow the workflow in references/instructions.md, " +
                    "validate using references/validation.md, and use references/examples.md when examples are needed.\""
            )
        )
    }

    @Test
    fun `escapes quoted yaml strings`() {
        val content = OpenAiYamlRenderer().render(
            testDefinition(
                name = "Quote \"Skill\"",
                description = "Use \"quotes\" safely",
            )
        )

        assertTrue(content.contains("display_name: \"Quote \\\"Skill\\\"\""))
        assertTrue(content.contains("short_description: \"Use \\\"quotes\\\" safely\""))
    }
}
