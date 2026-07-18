package com.spmisha134.skillops.generator

import com.spmisha134.skillops.model.generation.SkillGenerationRequest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import com.spmisha134.skillops.model.skill.SkillPlatform

class SkillGeneratorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `generates required files under project root even when selected directory is nested`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val selectedDirectory = Files.createDirectories(projectRoot.resolve("src/main/kotlin"))
        val result = SkillGenerator().generate(
            SkillGenerationRequest(
                projectBasePath = projectRoot.toString(),
                selectedDirectoryPath = selectedDirectory.toString(),
                skillDefinition = testDefinition()
            )
        )

        val skillDirectory = projectRoot.resolve(".agents/skills/test-skill")
        assertTrue(Files.isDirectory(skillDirectory))
        assertTrue(result.skillDirectoryPath.endsWith(".agents/skills/test-skill"))
        assertTrue(Files.exists(skillDirectory.resolve("SKILL.md")))
        assertTrue(Files.exists(skillDirectory.resolve("references/instructions.md")))
        assertTrue(Files.exists(skillDirectory.resolve("references/validation.md")))
        assertTrue(Files.exists(skillDirectory.resolve("references/examples.md")))
        assertTrue(Files.isDirectory(skillDirectory.resolve("scripts")))
        assertTrue(Files.isDirectory(skillDirectory.resolve("assets")))
        assertTrue(Files.exists(skillDirectory.resolve("agents/openai.yaml")))
    }

    @Test
    fun `skips optional scripts and assets only when disabled`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        SkillGenerator().generate(
            SkillGenerationRequest(
                projectBasePath = projectRoot.toString(),
                selectedDirectoryPath = projectRoot.toString(),
                skillDefinition = testDefinition(
                    createScriptsDirectory = false,
                    createAssetsDirectory = false,
                )
            )
        )

        val skillDirectory = projectRoot.resolve(".agents/skills/test-skill")
        assertFalse(Files.exists(skillDirectory.resolve("scripts")))
        assertFalse(Files.exists(skillDirectory.resolve("assets")))
        assertTrue(Files.exists(skillDirectory.resolve("agents/openai.yaml")))
    }

    @Test
    fun `generated content includes starter references and openai interface`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        SkillGenerator().generate(
            SkillGenerationRequest(
                projectBasePath = projectRoot.toString(),
                selectedDirectoryPath = projectRoot.toString(),
                skillDefinition = testDefinition()
            )
        )

        val skillDirectory = projectRoot.resolve(".agents/skills/test-skill")
        assertTrue(Files.readString(skillDirectory.resolve("SKILL.md")).contains("AGENTS.md"))
        assertTrue(Files.readString(skillDirectory.resolve("references/instructions.md")).contains("## Required workflow"))
        assertTrue(Files.readString(skillDirectory.resolve("references/validation.md")).contains("- [ ]"))
        assertTrue(Files.readString(skillDirectory.resolve("references/examples.md")).contains("## Good example"))
        assertTrue(Files.readString(skillDirectory.resolve("agents/openai.yaml")).contains("interface:"))
        assertTrue(Files.readString(skillDirectory.resolve("agents/openai.yaml")).contains("\$test-skill"))
    }

    @Test(expected = SkillGenerationException::class)
    fun `rejects duplicate skill directory`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val request = SkillGenerationRequest(
            projectBasePath = projectRoot.toString(),
            selectedDirectoryPath = projectRoot.toString(),
            skillDefinition = testDefinition()
        )

        SkillGenerator().generate(request)
        SkillGenerator().generate(request)
    }

    @Test
    fun `generates claude and gemini skills in platform directories without codex metadata`() {
        val projectRoot = temporaryFolder.newFolder("multi-platform-project").toPath()

        listOf(
            SkillPlatform.CLAUDE to ".claude",
            SkillPlatform.GEMINI to ".gemini",
        ).forEach { (platform, directory) ->
            SkillGenerator().generate(
                SkillGenerationRequest(
                    projectBasePath = projectRoot.toString(),
                    selectedDirectoryPath = projectRoot.toString(),
                    skillDefinition = testDefinition(name = "${platform.name.lowercase()} skill"),
                    platform = platform,
                )
            )

            val skillDirectory = projectRoot.resolve("$directory/skills/${platform.name.lowercase()}-skill")
            assertTrue(Files.exists(skillDirectory.resolve("SKILL.md")))
            assertTrue(Files.exists(skillDirectory.resolve("references/instructions.md")))
            assertFalse(Files.exists(skillDirectory.resolve("agents/openai.yaml")))
        }
    }
}
