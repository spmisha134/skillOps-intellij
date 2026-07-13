package com.spmisha134.skillops.insights.run

import com.spmisha134.skillops.insights.parser.CodexJsonlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files

class SkillUsageMatcherTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `matches generated skill path in session text`() {
        val events = parseEvents(
            """{"type":"message","payload":{"text":"Read .agents/skills/kafka-review/SKILL.md first"}}""",
        )

        val match = SkillUsageMatcher().matchSkill(events, listOf("api-helper", "kafka-review"))

        assertEquals("kafka-review", match)
    }

    @Test
    fun `returns null when no skill is referenced`() {
        val events = parseEvents(
            """{"type":"message","payload":{"text":"ordinary coding task"}}""",
        )

        val match = SkillUsageMatcher().matchSkill(events, listOf("kafka-review"))

        assertNull(match)
    }

    private fun parseEvents(vararg lines: String) =
        CodexJsonlParser().parse(
            temporaryFolder.newFile("session.jsonl").toPath().also {
                Files.writeString(it, lines.joinToString(separator = "\n", postfix = "\n"))
            }
        ).events
}
