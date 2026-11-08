package com.spmisha134.skillops.insights.presentation

import com.spmisha134.skillops.insights.run.SkillOpsRunInsightsReport
import com.spmisha134.skillops.insights.run.EfficiencySummary
import com.spmisha134.skillops.insights.run.SkillRunInsight
import com.spmisha134.skillops.insights.usage.TokenUsage
import com.spmisha134.skillops.insights.usage.RateLimitWindow
import com.spmisha134.skillops.insights.usage.UsageCredits
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Path

class RunInsightsReportFormatterTest {
    @Test
    fun `formats latest token usage and efficiency summary`() {
        val report = SkillOpsRunInsightsReport(
            insights = listOf(
                SkillRunInsight(
                    sessionPath = Path.of("/tmp/session.jsonl"),
                    sessionFileName = "session.jsonl",
                    lastModifiedMs = 1_000,
                    sizeBytes = 300,
                    matchedSkillName = "kafka-review",
                    tokenUsage = TokenUsage(
                        inputTokens = 100,
                        outputTokens = 25,
                        cachedInputTokens = 50,
                        reasoningOutputTokens = 5,
                        totalTokens = 125,
                        rateLimitUsedPercent = null,
                        rateLimitResetAt = null,
                        rawEvidence = emptyMap<String, Any?>(),
                        cacheCreationInputTokens = null,
                        toolTokens = null,
                        lastTurnInputTokens = 80,
                        lastTurnOutputTokens = 20,
                        lastTurnTotalTokens = 100,
                        contextWindowTokens = 200,
                        modelName = "test-model",
                        durationMs = 1_250,
                        turnCount = 3,
                        lastTurnCachedInputTokens = 70,
                        lastTurnReasoningOutputTokens = 4,
                        primaryRateLimit = RateLimitWindow(20.0, 300, 1_784_974_038),
                        secondaryRateLimit = RateLimitWindow(3.0, 10_080, 1_785_578_838),
                        usageCredits = UsageCredits(hasCredits = false, unlimited = false, balance = "0"),
                        planType = "plus",
                    ),
                    efficiencySummary = EfficiencySummary(
                        outputInputRatio = 0.25,
                        cachedInputPercent = 50.0,
                        reasoningOutputPercent = 20.0,
                        searchCount = 2,
                        warnings = emptyList(),
                    ),
                    warnings = emptyList(),
                )
            ),
            warnings = emptyList(),
        )

        val text = RunInsightsReportFormatter().format(report)

        assertTrue(text.contains("Skill: kafka-review"))
        assertTrue(text.contains("- Tokens used in this session: 125"))
        assertTrue(text.contains("- Latest recorded context size: 100 tokens"))
        assertTrue(text.contains("- Model context capacity: 200 tokens (100 approximate tokens left at last snapshot)"))
        assertTrue(text.contains("- Model: test-model"))
        assertTrue(text.contains("- Duration: 1.2s"))
        assertTrue(text.contains("- Turns: 3"))
        assertTrue(text.contains("- 5-hour limit: 20.0% used"))
        assertTrue(text.contains("- Weekly limit: 3.0% used"))
        assertTrue(text.contains("- Credit balance: 0"))
        assertTrue(text.contains("- Output/input ratio: 0.25"))
        assertTrue(text.contains("- Cached input: 50.0%"))
        assertTrue(!text.contains("Session: session.jsonl"))
        assertTrue(!text.contains("Setup warnings:"))

        val metrics = RunInsightsReportFormatter().metrics(report.latestInsight!!, "Codex")
        assertTrue(metrics.any { it.section == "Latest context" && it.name == "Cached input" && it.value == "70" })
        assertTrue(metrics.any { it.name == "5-hour limit usage" && it.value.contains("80.0% left") })
        assertTrue(metrics.any { it.name == "Weekly limit reset" })
        assertTrue(metrics.any { it.section == "Credits" && it.name == "Balance" && it.value == "0" })
    }

    @Test
    fun `groups sessions without skills under no skill`() {
        val insight = SkillRunInsight(
            sessionPath = Path.of("/tmp/session.jsonl"),
            sessionFileName = "session.jsonl",
            lastModifiedMs = 1_000,
            sizeBytes = 0,
            matchedSkillName = null,
            tokenUsage = null,
            efficiencySummary = EfficiencySummary(null, null, null, 0, emptyList()),
            warnings = emptyList(),
        )

        val label = RunInsightsReportFormatter().selectionLabel(insight)
        val text = RunInsightsReportFormatter().format(insight)

        assertTrue(label == "No skill")
        assertTrue(text.contains("Skill: No skill"))
        assertTrue(!text.contains("session.jsonl"))
    }

    @Test
    fun `uses one no skill dropdown option for separate ordinary runs`() {
        val formatter = RunInsightsReportFormatter()
        val insights = listOf(1_000L, 2_000L).map { modified ->
            SkillRunInsight(
                sessionPath = Path.of("/tmp/session-$modified.jsonl"),
                sessionFileName = "session-$modified.jsonl",
                lastModifiedMs = modified,
                sizeBytes = 0,
                matchedSkillName = null,
                tokenUsage = null,
                efficiencySummary = EfficiencySummary(null, null, null, 0, emptyList()),
                warnings = emptyList(),
            )
        }

        val dropdownOptions = insights.flatMap(formatter::skillNames).distinctBy(String::lowercase)

        assertTrue(dropdownOptions == listOf("No skill"))
        assertTrue(insights.map(formatter::runLabel).distinct().size == 2)
    }

    @Test
    fun `uses recorded skill name when repository match is unavailable`() {
        val insight = SkillRunInsight(
            sessionPath = Path.of("/tmp/session.jsonl"),
            sessionFileName = "session.jsonl",
            lastModifiedMs = 1_000,
            sizeBytes = 0,
            matchedSkillName = null,
            tokenUsage = null,
            efficiencySummary = EfficiencySummary(null, null, null, 0, emptyList()),
            warnings = emptyList(),
            recordedSkillNames = listOf("external-helper"),
        )

        val formatter = RunInsightsReportFormatter()

        assertTrue(formatter.selectionLabel(insight) == "external-helper")
        assertTrue(formatter.format(insight)
            .contains("Skill: external-helper"))
    }

    @Test
    fun `formats warning as note with improvement advice`() {
        val insight = SkillRunInsight(
            sessionPath = Path.of("/tmp/session.jsonl"),
            sessionFileName = "session.jsonl",
            lastModifiedMs = 1_000,
            sizeBytes = 127_805,
            matchedSkillName = "update-deps",
            tokenUsage = null,
            efficiencySummary = EfficiencySummary(null, null, null, 1, emptyList()),
            warnings = listOf("Transcript size warning: 127805 bytes may slow scanning and increase context overhead."),
        )

        val text = RunInsightsReportFormatter().format(insight)

        assertTrue(text.contains("Notes"))
        assertTrue(text.contains("How to improve: Start a new Codex session"))
        assertTrue(!text.contains("Ignored 83 sessions"))
    }

    @Test
    fun `uses provider name in improvement advice`() {
        val insight = SkillRunInsight(
            sessionPath = Path.of("/tmp/session.jsonl"),
            sessionFileName = "session.jsonl",
            lastModifiedMs = 1_000,
            sizeBytes = 60_506,
            matchedSkillName = null,
            tokenUsage = null,
            efficiencySummary = EfficiencySummary(null, null, null, 0, emptyList()),
            warnings = listOf("Transcript size warning: 60506 bytes may slow scanning and increase context overhead."),
        )

        val text = RunInsightsReportFormatter().format(insight, platformName = "Claude")

        assertTrue(text.contains("How to improve: Start a new Claude session"))
        assertTrue(!text.contains("Codex session"))
    }

    @Test
    fun `formats invocation command and run history label`() {
        val insight = SkillRunInsight(
            sessionPath = Path.of("/tmp/session.jsonl"),
            sessionFileName = "session.jsonl",
            lastModifiedMs = 1_000,
            sizeBytes = 0,
            matchedSkillName = "update-deps",
            tokenUsage = null,
            efficiencySummary = EfficiencySummary(null, null, null, 0, emptyList()),
            warnings = emptyList(),
            invocationCommand = "${'$'}update-deps run the skill",
        )
        val formatter = RunInsightsReportFormatter()

        val text = formatter.format(insight)

        assertTrue(text.contains("Prompts:"))
        assertTrue(text.contains("1. ${'$'}update-deps run the skill"))
        assertTrue(formatter.runLabel(insight).isNotBlank())
    }
}
