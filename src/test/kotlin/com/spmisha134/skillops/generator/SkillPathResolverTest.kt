package com.spmisha134.skillops.generator

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Path
import com.spmisha134.skillops.model.skill.SkillPlatform

class SkillPathResolverTest {
    @Test
    fun `resolves skill directory under project root`() {
        val projectRoot = Path.of("/tmp/project")
        val result = SkillPathResolver().resolveSkillDirectory(projectRoot.toString(), "My Skill")

        assertEquals(projectRoot.resolve(".agents").resolve("skills").resolve("my-skill"), result)
    }

    @Test
    fun `resolves platform specific project skill directories`() {
        val projectRoot = Path.of("/tmp/project")
        val resolver = SkillPathResolver()

        assertEquals(projectRoot.resolve(".agents/skills/my-skill"), resolver.resolveSkillDirectory(projectRoot.toString(), "my-skill", SkillPlatform.CODEX))
        assertEquals(projectRoot.resolve(".claude/skills/my-skill"), resolver.resolveSkillDirectory(projectRoot.toString(), "my-skill", SkillPlatform.CLAUDE))
        assertEquals(projectRoot.resolve(".gemini/skills/my-skill"), resolver.resolveSkillDirectory(projectRoot.toString(), "my-skill", SkillPlatform.GEMINI))
    }
}
