package com.spmisha134.skillops.validator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

class SkillPathDetectorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val detector = SkillPathDetector()

    @Test
    fun `selecting a skill directory validates only that skill`() {
        val projectRoot = projectRootWithSkills("alpha-skill", "beta-skill")
        val selectedSkill = projectRoot.resolve(".agents/skills/alpha-skill")

        val result = detector.skillDirectoriesToValidate(projectRoot, selectedSkill)

        assertEquals(listOf(selectedSkill), result)
    }

    @Test
    fun `selecting skills root validates child skill directories`() {
        val projectRoot = projectRootWithSkills("beta-skill", "alpha-skill")
        val skillsRoot = projectRoot.resolve(".agents/skills")
        skillsRoot.resolve("notes.txt").writeText("not a skill directory")

        val result = detector.skillDirectoriesToValidate(projectRoot, skillsRoot)

        assertEquals(
            listOf(
                skillsRoot.resolve("alpha-skill"),
                skillsRoot.resolve("beta-skill"),
            ),
            result
        )
    }

    @Test
    fun `selecting project root validates all skills when skills root exists`() {
        val projectRoot = projectRootWithSkills("alpha-skill", "beta-skill")

        val result = detector.skillDirectoriesToValidate(projectRoot, projectRoot)

        assertEquals(
            listOf(
                projectRoot.resolve(".agents/skills/alpha-skill"),
                projectRoot.resolve(".agents/skills/beta-skill"),
            ),
            result
        )
    }

    @Test
    fun `selecting unrelated directory returns no skill directories`() {
        val projectRoot = projectRootWithSkills("alpha-skill")
        val unrelated = Files.createDirectories(projectRoot.resolve("src/main"))

        val result = detector.skillDirectoriesToValidate(projectRoot, unrelated)

        assertTrue(result.isEmpty())
    }

    private fun projectRootWithSkills(vararg skillNames: String): Path {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillsRoot = Files.createDirectories(projectRoot.resolve(".agents/skills"))
        skillNames.forEach { skillName ->
            Files.createDirectories(skillsRoot.resolve(skillName))
        }
        return projectRoot
    }
}
