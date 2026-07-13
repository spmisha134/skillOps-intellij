package com.spmisha134.skillops.validator.rules.structure

import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.model.validation.SkillValidationSeverity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import kotlin.io.path.writeText

class FrontMatterRuleTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `reports missing front matter`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = Files.createDirectories(projectRoot.resolve(".agents/skills/test-skill"))
        skillDirectory.resolve("SKILL.md").writeText("No front matter")

        val issues = FrontMatterRule().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("front matter") })
    }
}
