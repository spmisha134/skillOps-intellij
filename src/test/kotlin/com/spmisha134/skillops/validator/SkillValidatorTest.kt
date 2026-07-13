package com.spmisha134.skillops.validator

import com.spmisha134.skillops.generator.SkillGenerator
import com.spmisha134.skillops.generator.testDefinition
import com.spmisha134.skillops.model.generation.SkillGenerationRequest
import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.model.validation.SkillValidationSeverity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

class SkillValidatorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `valid generated skill has no validation issues`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = generateSkill(projectRoot)

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertFalse(result.hasErrors)
        assertFalse(result.hasWarnings)
    }

    @Test
    fun `missing skill md is an error`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = Files.createDirectories(projectRoot.resolve(".agents/skills/broken-skill"))
        Files.createDirectories(skillDirectory.resolve("references"))

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("SKILL.md") })
    }

    @Test
    fun `missing and blank references are errors`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = generateSkill(projectRoot)
        Files.delete(skillDirectory.resolve("references/examples.md"))
        skillDirectory.resolve("references/instructions.md").writeText("")

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("references/examples.md") })
        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("references/instructions.md") })
    }

    @Test
    fun `large skill md is a warning`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = generateSkill(projectRoot)
        skillDirectory.resolve("SKILL.md").writeText(
            """
                ---
                name: test-skill
                description: Test
                ---

                AGENTS.md agents.md CLAUDE.md claude.md GEMINI.md gemini.md

            """.trimIndent() + "x".repeat(1300)
        )

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.WARNING && it.message.contains("larger") })
    }

    @Test
    fun `missing project instruction discovery is an error`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = generateSkill(projectRoot)
        skillDirectory.resolve("SKILL.md").writeText(
            """
                ---
                name: test-skill
                description: Test
                ---

                Follow the workflow.
            """.trimIndent()
        )

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("project instruction discovery") })
    }

    @Test
    fun `unsafe folder name is an error`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = generateSkill(projectRoot)
        val unsafeDirectory = Files.move(skillDirectory, skillDirectory.parent.resolve("Unsafe Skill"))

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, unsafeDirectory))

        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("normalized") })
    }

    @Test
    fun `missing openai yaml is an error`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = generateSkill(projectRoot)
        Files.delete(skillDirectory.resolve("agents/openai.yaml"))

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("agents/openai.yaml") })
    }

    @Test
    fun `incomplete openai yaml is an error`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = generateSkill(projectRoot)
        skillDirectory.resolve("agents/openai.yaml").writeText("interface:\n  display_name: \"Test Skill\"\n")

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("short_description") })
        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("default_prompt") })
        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("\$test-skill") })
    }

    @Test
    fun `folder outside agents skills is an error`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = Files.createDirectories(projectRoot.resolve("other/test-skill"))

        val result = SkillValidator().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(result.issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains(".agents/skills") })
    }

    private fun generateSkill(projectRoot: Path): Path {
        val result = SkillGenerator().generate(
            SkillGenerationRequest(
                projectBasePath = projectRoot.toString(),
                selectedDirectoryPath = projectRoot.toString(),
                skillDefinition = testDefinition()
            )
        )
        return Path.of(result.skillDirectoryPath)
    }
}
