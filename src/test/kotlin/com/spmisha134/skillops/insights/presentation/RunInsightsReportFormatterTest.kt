package com.spmisha134.skillops.insights.presentation

import com.spmisha134.skillops.insights.run.SkillOpsRunInsightsReport
import com.spmisha134.skillops.insights.run.EfficiencySummary
import com.spmisha134.skillops.insights.run.SkillRunInsight
import com.spmisha134.skillops.insights.usage.TokenUsage
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
                        rawEvidence = emptyMap(),
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

        assertTrue(text.contains("SkillOps Run Insights"))
        assertTrue(text.contains("Skill: kafka-review"))
        assertTrue(text.contains("total 125"))
        assertTrue(text.contains("output/input 0.25"))
        assertTrue(text.contains("cached input 50.0%"))
    }
}
