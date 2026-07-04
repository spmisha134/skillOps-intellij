package com.spmisha134.buildskill.generator

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Path

class SkillPathResolverTest {
    @Test
    fun `resolves skill directory under project root`() {
        val projectRoot = Path.of("/tmp/project")
        val result = SkillPathResolver().resolveSkillDirectory(projectRoot.toString(), "My Skill")

        assertEquals(projectRoot.resolve(".agents").resolve("skills").resolve("my-skill"), result)
    }
}
