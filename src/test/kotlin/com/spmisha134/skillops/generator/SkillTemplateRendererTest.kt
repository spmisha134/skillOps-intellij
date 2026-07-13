package com.spmisha134.skillops.generator

import org.junit.Assert.assertTrue
import org.junit.Test

class SkillTemplateRendererTest {
    @Test
    fun `renders small skill entrypoint with project instruction discovery`() {
        val content = SkillTemplateRenderer().render(testDefinition())

        assertTrue(content.startsWith("---\nname: test-skill\ndescription: Use for tests\n---"))
        assertTrue(content.length < 1200)
        assertTrue(content.contains("AGENTS.md"))
        assertTrue(content.contains("CLAUDE.md"))
        assertTrue(content.contains("GEMINI.md"))
        assertTrue(content.contains("references/instructions.md"))
        assertTrue(content.contains("references/validation.md"))
        assertTrue(content.contains("references/examples.md"))
    }
}
