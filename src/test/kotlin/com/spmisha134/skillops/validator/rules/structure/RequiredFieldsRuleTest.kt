package com.spmisha134.skillops.validator.rules.structure

import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.model.validation.SkillValidationSeverity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import kotlin.io.path.writeText

class RequiredFieldsRuleTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `reports missing name and blank description`() {
        val projectRoot = temporaryFolder.newFolder("project").toPath()
        val skillDirectory = Files.createDirectories(projectRoot.resolve(".agents/skills/test-skill"))
        skillDirectory.resolve("SKILL.md").writeText(
            """
                ---
                description:
                ---
            """.trimIndent()
        )

        val issues = RequiredFieldsRule().validate(SkillValidationContext(projectRoot, skillDirectory))

        assertTrue(issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("name") })
        assertTrue(issues.any { it.severity == SkillValidationSeverity.ERROR && it.message.contains("description") })
    }
}
