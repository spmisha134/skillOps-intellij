package com.spmisha134.skillops.insights.presentation

import com.spmisha134.skillops.insights.run.SkillOpsRunInsightsReport
import com.spmisha134.skillops.insights.run.SkillRunInsight
import com.spmisha134.skillops.insights.usage.TokenUsage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RunInsightsReportFormatter {
    fun format(report: SkillOpsRunInsightsReport): String {
        val lines = mutableListOf<String>()
        lines += "SkillOps Run Insights"
        lines += ""

        if (report.warnings.isNotEmpty()) {
            lines += "Setup warnings:"
            report.warnings.forEach { warning -> lines += "- $warning" }
            lines += ""
        }

        if (report.insights.isEmpty()) {
            lines += "No Codex sessions were found."
            return lines.joinToString("\n")
        }

        report.latestInsight?.let { latest ->
            lines += "Latest session"
            lines += "Skill: ${latest.matchedSkillName ?: "No SkillOps skill detected"}"
            lines += "Session: ${latest.sessionFileName}"
            lines += "Updated: ${formatInstant(latest.lastModifiedMs)}"
            lines += "Tokens: ${latest.tokenUsage.formatTokens()}"
            lines += "Efficiency: ${latest.formatEfficiency()}"
            latest.warnings.forEach { warning -> lines += "Warning: $warning" }
            lines += ""
        }

        lines += "Recent sessions"
        report.insights.drop(1).take(MAX_RECENT_SESSIONS).forEach { insight ->
            lines += "- ${insight.sessionFileName}: ${insight.matchedSkillName ?: "no skill detected"}, ${insight.tokenUsage.formatTokens()}"
        }

        return lines.joinToString("\n")
    }

    private fun SkillRunInsight.formatEfficiency(): String {
        val parts = listOfNotNull(
            efficiencySummary.outputInputRatio?.let { "output/input ${formatDecimal(it)}" },
            efficiencySummary.cachedInputPercent?.let { "cached input ${formatPercent(it)}" },
            efficiencySummary.reasoningOutputPercent?.let { "reasoning output ${formatPercent(it)}" },
            "searches ${efficiencySummary.searchCount}",
        )
        return parts.joinToString(", ")
    }

    private fun TokenUsage?.formatTokens(): String {
        if (this == null) {
            return "not found"
        }

        val parts = listOfNotNull(
            totalTokens?.let { "total ${formatLong(it)}" },
            inputTokens?.let { "input ${formatLong(it)}" },
            outputTokens?.let { "output ${formatLong(it)}" },
            cachedInputTokens?.let { "cached ${formatLong(it)}" },
            reasoningOutputTokens?.let { "reasoning ${formatLong(it)}" },
            rateLimitUsedPercent?.let { "rate limit ${formatPercent(it)}" },
        )
        return parts.ifEmpty { listOf("token event found, no totals") }.joinToString(", ")
    }

    private fun formatLong(value: Long): String =
        "%,d".format(value)

    private fun formatDecimal(value: Double): String =
        "%.2f".format(value)

    private fun formatPercent(value: Double): String =
        "%.1f%%".format(value)

    private fun formatInstant(epochMs: Long): String =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
            Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault())
        )

    companion object {
        private const val MAX_RECENT_SESSIONS = 9
    }
}
