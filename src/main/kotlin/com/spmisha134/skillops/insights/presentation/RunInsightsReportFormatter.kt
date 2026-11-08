package com.spmisha134.skillops.insights.presentation

import com.spmisha134.skillops.insights.run.SkillOpsRunInsightsReport
import com.spmisha134.skillops.insights.run.SkillRunInsight
import com.spmisha134.skillops.insights.usage.RateLimitWindow
import com.spmisha134.skillops.insights.usage.TokenUsage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RunInsightsReportFormatter {
    fun format(report: SkillOpsRunInsightsReport): String {
        val selectedInsight = report.latestInsight
        if (selectedInsight != null) {
            return format(selectedInsight, report.platformName)
        }

        val lines = mutableListOf<String>()
        lines += "SkillOps ${report.platformName} Run Insights"
        lines += ""

        if (report.warnings.isNotEmpty()) {
            lines += "Setup warnings:"
            report.warnings.forEach { warning -> lines += "- $warning" }
            lines += ""
        }

        lines += "No ${report.platformName} sessions were found for this project."
        return lines.joinToString("\n")
    }

    fun format(insight: SkillRunInsight, platformName: String = "Codex"): String {
        val lines = mutableListOf<String>()
        lines += "Skill: ${skillNames(insight).joinToString()}"
        lines += "Updated: ${formatInstant(insight.lastModifiedMs)}"
        insight.resumeTarget?.let { lines += "Session: ${it.sessionId}" }
        lines += ""
        lines += "Tokens"
        lines += insight.tokenUsage.formatTokenLines(platformName)
        lines += ""
        lines += "Efficiency"
        lines += insight.formatEfficiencyLines()

        val prompts = insight.prompts.ifEmpty { listOfNotNull(insight.invocationCommand) }
        if (prompts.isNotEmpty()) {
            lines += ""
            lines += "Prompts:"
            prompts.forEachIndexed { index, prompt ->
                lines += "${index + 1}. $prompt"
            }
        }

        if (insight.warnings.isNotEmpty()) {
            lines += ""
            lines += "Notes"
            insight.warnings.forEach { warning ->
                lines += "- $warning"
                lines += "  How to improve: ${warning.improvementAdvice(platformName)}"
            }
        }

        return lines.joinToString("\n")
    }

    fun selectionLabel(insight: SkillRunInsight): String {
        return skillNames(insight).joinToString()
    }

    fun skillNames(insight: SkillRunInsight): List<String> =
        insight.displaySkillNames().ifEmpty { listOf(NO_SKILL_LABEL) }

    fun runLabel(insight: SkillRunInsight): String = formatInstant(insight.lastModifiedMs)

    fun metrics(insight: SkillRunInsight, platformName: String): List<InsightMetric> {
        val usage = insight.tokenUsage
        val rows = mutableListOf<InsightMetric>()
        fun metric(section: String, name: String, value: Any?, explanation: String) {
            value?.let { rows += InsightMetric(section, name, it.toString(), explanation) }
        }
        fun token(section: String, name: String, value: Long?, explanation: String) {
            value?.let { metric(section, name, formatLong(it), explanation) }
        }

        token("Session tokens", "Total", usage?.totalTokens, "Cumulative input and output tokens recorded for this session.")
        token("Session tokens", "Input", usage?.inputTokens, "Context, instructions, prompts, files, and tool results sent to the model.")
        token("Session tokens", "Output", usage?.outputTokens, "Tokens produced for responses, tool calls, and reported reasoning.")
        token("Session tokens", "Cached input", usage?.cachedInputTokens, "Input reused from the provider cache; this is part of the input total.")
        token("Session tokens", "Cache write", usage?.cacheCreationInputTokens, "Input written into the provider cache.")
        token("Session tokens", "Reasoning output", usage?.reasoningOutputTokens, "Provider-reported reasoning tokens included in output usage.")
        token("Session tokens", "Tool", usage?.toolTokens, "Provider-reported tokens used by tool calls.")

        val latestSection = if (platformName.equals("Claude", ignoreCase = true)) "Latest assistant call" else "Latest context"
        token(latestSection, "Total", usage?.lastTurnTotalTokens, "Token usage in the latest provider snapshot, not the cumulative session total.")
        token(latestSection, "Input", usage?.lastTurnInputTokens, "Input tokens in the latest provider snapshot.")
        token(latestSection, "Output", usage?.lastTurnOutputTokens, "Output tokens in the latest provider snapshot.")
        token(latestSection, "Cached input", usage?.lastTurnCachedInputTokens, "Cached input tokens in the latest provider snapshot.")
        token(latestSection, "Cache write", usage?.lastTurnCacheCreationInputTokens, "Cache-write tokens in the latest provider snapshot.")
        token(latestSection, "Reasoning output", usage?.lastTurnReasoningOutputTokens, "Reasoning tokens in the latest provider snapshot.")

        token("Context", "Model capacity", usage?.contextWindowTokens, "Maximum context capacity reported by the provider.")
        usage?.contextWindowTokens?.let { capacity ->
            usage.lastTurnTotalTokens?.let { used ->
                token("Context", "Approximate capacity left", (capacity - used).coerceAtLeast(0), "Estimated capacity at the latest snapshot; not account quota or billing allowance.")
            }
        }

        usage?.modelName?.let { metric("Session", "Model", it, "Model recorded in the session transcript.") }
        usage?.durationMs?.let { metric("Session", "Duration", formatDuration(it), "Provider-recorded session duration.") }
        usage?.turnCount?.let { metric("Session", "Turns", it, "Number of recorded turns or assistant calls.") }
        usage?.planType?.let { metric("Limits", "Plan", it, "Subscription plan recorded by Codex.") }
        usage?.rateLimitName?.let { metric("Limits", "Limit name", it, "Named model or limit pool reported by Codex.") }
        usage?.rateLimitId?.let { metric("Limits", "Limit ID", it, "Provider identifier for the active rate-limit pool.") }
        addRateLimitMetrics(rows, usage?.primaryRateLimit, "Primary limit")
        addRateLimitMetrics(rows, usage?.secondaryRateLimit, "Secondary limit")
        if (usage?.primaryRateLimit == null) {
            usage?.rateLimitUsedPercent?.let { metric("Limits", "Provider usage", formatPercent(it), "Provider rate-limit usage from a legacy transcript.") }
            usage?.rateLimitResetAt?.let { metric("Limits", "Provider reset", it, "Reset time reported by a legacy transcript.") }
        }
        usage?.rateLimitRemainingTokens?.let { remaining ->
            val value = formatLong(remaining) + usage.rateLimitLimitTokens?.let { " of ${formatLong(it)}" }.orEmpty()
            metric("Limits", "Tokens remaining", value, "Token allowance reported by a legacy provider record.")
        }
        usage?.usageCredits?.let { credits ->
            credits.balance?.let { metric("Credits", "Balance", it, "Credit balance recorded by Codex.") }
            credits.hasCredits?.let { metric("Credits", "Available", yesNo(it), "Whether Codex reports usable credits.") }
            credits.unlimited?.let { metric("Credits", "Unlimited", yesNo(it), "Whether the credit allowance is unlimited.") }
        }
        usage?.rateLimitReachedType?.let { metric("Limits", "Reached limit", it, "The rate-limit category currently reached.") }
        usage?.spendControlReached?.let { metric("Limits", "Spend control reached", yesNo(it), "Whether the account spend control has been reached.") }

        metric("Activity", "Searches", insight.efficiencySummary.searchCount, "Detected repository or web-search activity.")
        metric("Activity", "Tool calls", insight.efficiencySummary.toolCallCount, "Detected tool invocations in the transcript.")
        usage?.serverWebSearchRequests?.let { metric("Activity", "Server web searches", it, "Provider-recorded server web-search calls.") }
        usage?.serverWebFetchRequests?.let { metric("Activity", "Server web fetches", it, "Provider-recorded server web-fetch calls.") }
        return rows
    }

    private fun addRateLimitMetrics(rows: MutableList<InsightMetric>, window: RateLimitWindow?, fallbackName: String) {
        if (window == null) return
        val name = rateLimitWindowName(window.windowMinutes, fallbackName)
        window.usedPercent?.let { used ->
            rows += InsightMetric(
                "Limits",
                "$name usage",
                "${formatPercent(used)} used · ${formatPercent((100.0 - used).coerceIn(0.0, 100.0))} left",
                "Usage in the provider's rolling ${formatWindow(window.windowMinutes)} window.",
            )
        }
        window.resetsAtEpochSeconds?.let { reset ->
            rows += InsightMetric("Limits", "$name reset", formatInstant(reset * 1_000), "Local time when this limit window resets.")
        }
    }

    private fun SkillRunInsight.displaySkillNames(): List<String> =
        (matchedSkillNames + recordedSkillNames).distinctBy(String::lowercase)

    private fun SkillRunInsight.formatEfficiencyLines(): List<String> =
        listOfNotNull(
            efficiencySummary.outputInputRatio?.let { "- Output/input ratio: ${formatDecimal(it)}" },
            efficiencySummary.cachedInputPercent?.let { "- Cached input: ${formatPercent(it)}" },
            efficiencySummary.reasoningOutputPercent?.let { "- Reasoning output: ${formatPercent(it)}" },
            "- Searches: ${efficiencySummary.searchCount}",
            "- Tool calls: ${efficiencySummary.toolCallCount}",
        )

    private fun TokenUsage?.formatTokenLines(platformName: String): List<String> {
        if (this == null) {
            return listOf("- Not available")
        }

        val legacyRateLimitUsage = rateLimitUsedPercent.takeIf { primaryRateLimit == null }
        val legacyRateLimitReset = rateLimitResetAt.takeIf { primaryRateLimit == null }

        val latestUsageLines = if (platformName.equals("Claude", ignoreCase = true)) {
            listOfNotNull(
                lastTurnInputTokens?.let { "- Latest recorded assistant call input tokens: ${formatLong(it)}" },
                lastTurnOutputTokens?.let { "- Latest recorded assistant call output tokens: ${formatLong(it)}" },
                lastTurnCachedInputTokens?.let { "- Latest recorded assistant call cached input tokens: ${formatLong(it)}" },
                lastTurnReasoningOutputTokens?.let { "- Latest recorded assistant call reasoning tokens: ${formatLong(it)}" },
                lastTurnTotalTokens?.let { "- Latest recorded assistant call total tokens: ${formatLong(it)}" },
            )
        } else {
            listOfNotNull(
                lastTurnInputTokens?.let { "- Latest recorded context input tokens: ${formatLong(it)}" },
                lastTurnOutputTokens?.let { "- Latest recorded context output tokens: ${formatLong(it)}" },
                lastTurnCachedInputTokens?.let { "- Latest recorded context cached input tokens: ${formatLong(it)}" },
                lastTurnCacheCreationInputTokens?.let { "- Latest recorded context cache-write tokens: ${formatLong(it)}" },
                lastTurnReasoningOutputTokens?.let { "- Latest recorded context reasoning tokens: ${formatLong(it)}" },
                lastTurnTotalTokens?.let { "- Latest recorded context size: ${formatLong(it)} tokens" },
            )
        }

        val lines = listOfNotNull(
            totalTokens?.let { "- Tokens used in this session: ${formatLong(it)}" },
            inputTokens?.let { "- Tokens used for input: ${formatLong(it)}" },
            outputTokens?.let { "- Tokens used for output generation: ${formatLong(it)}" },
            cachedInputTokens?.let { "- Cached input tokens: ${formatLong(it)}" },
            cacheCreationInputTokens?.let { "- Cache creation tokens: ${formatLong(it)}" },
            toolTokens?.let { "- Tool-call tokens: ${formatLong(it)}" },
            reasoningOutputTokens?.let { "- Reasoning tokens: ${formatLong(it)}" },
            legacyRateLimitUsage?.let { "- Provider rate-limit usage: ${formatPercent(it)}" },
            rateLimitRemainingTokens?.let { remaining ->
                "- Provider rate-limit tokens remaining: ${formatLong(remaining)}" +
                    rateLimitLimitTokens?.let { limit -> " of ${formatLong(limit)}" }.orEmpty()
            },
            legacyRateLimitReset?.let { "- Provider rate-limit reset: $it" },
            planType?.let { "- Plan: $it" },
            rateLimitName?.let { "- Limit name: $it" },
            primaryRateLimit?.formatRateLimitLine("Primary limit"),
            secondaryRateLimit?.formatRateLimitLine("Secondary limit"),
            usageCredits?.balance?.let { "- Credit balance: $it" },
            modelName?.let { "- Model: $it" },
            durationMs?.let { "- Duration: ${formatDuration(it)}" },
            turnCount?.let { "- Turns: $it" },
            serverWebSearchRequests?.let { "- Server web searches: $it" },
            serverWebFetchRequests?.let { "- Server web fetches: $it" },
            contextWindowTokens?.let { window ->
                "- Model context capacity: ${formatLong(window)} tokens" +
                    lastTurnTotalTokens?.let { used -> " (${formatLong((window - used).coerceAtLeast(0))} approximate tokens left at last snapshot)" }.orEmpty()
            },
        ) + latestUsageLines
        return lines.ifEmpty { listOf("- Token event found, but totals were unavailable") }
    }

    private fun String.improvementAdvice(platformName: String): String = when {
        startsWith("Transcript size warning") ->
            "Start a new $platformName session for a separate task; avoid dumping large command output to limit scan time and context overhead."
        startsWith("High repository/search activity") ->
            "Narrow the request and point $platformName to the relevant files or package when they are known."
        startsWith("Rate limit usage is high") ->
            "Wait for the rate-limit window to reset or reduce repeated large-context requests."
        startsWith("No token usage event") ->
            "Let the assistant turn finish, then reopen Run Insights so its token usage can be read."
        else -> "Review the session scope and run a smaller, focused $platformName task."
    }

    private fun formatLong(value: Long): String =
        "%,d".format(value)

    private fun formatDecimal(value: Double): String =
        "%.2f".format(value)

    private fun formatPercent(value: Double): String =
        "%.1f%%".format(value)

    private fun formatDuration(value: Long): String =
        if (value >= 60_000) "${value / 60_000}m ${(value % 60_000) / 1_000}s" else "${value / 1_000}.${(value % 1_000) / 100}s"

    private fun RateLimitWindow.formatRateLimitLine(fallbackName: String): String {
        val name = rateLimitWindowName(windowMinutes, fallbackName)
        val details = listOfNotNull(
            usedPercent?.let { "${formatPercent(it)} used" },
            resetsAtEpochSeconds?.let { "resets ${formatInstant(it * 1_000)}" },
        ).joinToString(", ")
        return "- $name: $details"
    }

    private fun rateLimitWindowName(minutes: Long?, fallback: String): String = when (minutes) {
        300L -> "5-hour limit"
        10_080L -> "Weekly limit"
        else -> fallback
    }

    private fun formatWindow(minutes: Long?): String = when {
        minutes == null -> "reported"
        minutes % 10_080 == 0L -> "${minutes / 10_080}-week"
        minutes % 1_440 == 0L -> "${minutes / 1_440}-day"
        minutes % 60 == 0L -> "${minutes / 60}-hour"
        else -> "$minutes-minute"
    }

    private fun yesNo(value: Boolean): String = if (value) "Yes" else "No"

    private fun formatInstant(epochMs: Long): String =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
            Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault())
        )

    companion object {
        const val NO_SKILL_LABEL = "No skill"
    }

}
