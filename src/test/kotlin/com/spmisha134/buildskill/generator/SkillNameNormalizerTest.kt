package com.spmisha134.buildskill.generator

import org.junit.Assert.assertEquals
import org.junit.Test

class SkillNameNormalizerTest {
    @Test
    fun `normalizes common skill names`() {
        assertEquals("photoact-spec-writer", SkillNameNormalizer.normalize("PhotoAct Spec Writer"))
        assertEquals("kafka-mcp-tool", SkillNameNormalizer.normalize("Kafka MCP Tool"))
        assertEquals("my-skill", SkillNameNormalizer.normalize("  My Skill  "))
        assertEquals("backendapi-spec", SkillNameNormalizer.normalize("Backend/API Spec!!!"))
    }

    @Test
    fun `collapses repeated hyphens and trims them`() {
        assertEquals("my-skill", SkillNameNormalizer.normalize(" --My   Skill-- "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects empty normalized names`() {
        SkillNameNormalizer.normalize(" !!! ")
    }
}
