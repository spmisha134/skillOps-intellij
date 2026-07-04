package com.spmisha134.buildskill.generator

import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceTemplateRendererTest {
    @Test
    fun `renders non-empty reference files`() {
        val renderer = ReferenceTemplateRenderer()
        val definition = testDefinition()

        assertTrue(renderer.renderInstructions(definition).contains("## Required workflow"))
        assertTrue(renderer.renderInstructions(definition).contains("## Output guidance"))
        assertTrue(renderer.renderValidation(definition).contains("## Required checks"))
        assertTrue(renderer.renderValidation(definition).contains("- [ ]"))
        assertTrue(renderer.renderExamples(definition).contains("## Good example"))
        assertTrue(renderer.renderExamples(definition).contains("## Bad example"))
    }
}
