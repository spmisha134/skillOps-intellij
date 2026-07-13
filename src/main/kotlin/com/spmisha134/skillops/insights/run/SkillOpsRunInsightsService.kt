package com.spmisha134.skillops.insights.run

import com.spmisha134.skillops.insights.parser.CodexJsonlParser
import com.spmisha134.skillops.insights.session.CodexSessionFileScanner
import com.spmisha134.skillops.insights.settings.SkillOpsInsightsSettings
import com.spmisha134.skillops.insights.usage.TokenUsageExtractor
import java.nio.file.Path

class SkillOpsRunInsightsService(
    private val sessionFileScanner: CodexSessionFileScanner = CodexSessionFileScanner(),
    private val parser: CodexJsonlParser = CodexJsonlParser(),
    private val tokenUsageExtractor: TokenUsageExtractor = TokenUsageExtractor(),
    private val skillCatalog: SkillCatalog = SkillCatalog(),
    private val skillUsageMatcher: SkillUsageMatcher = SkillUsageMatcher(),
    private val efficiencySummaryCalculator: EfficiencySummaryCalculator = EfficiencySummaryCalculator(),
) {
    fun buildReport(
        projectRoot: Path,
        settings: SkillOpsInsightsSettings,
    ): SkillOpsRunInsightsReport {
        val normalizedSettings = settings.normalized()
        val scanResult = sessionFileScanner.scan(normalizedSettings)
        val skillNames = skillCatalog.discover(projectRoot)
        val warnings = scanResult.warnings.toMutableList()

        if (skillNames.isEmpty()) {
            warnings += "No SkillOps skills found under .agents/skills/."
        }

        val insights = scanResult.files.map { sessionFile ->
            val parseResult = parser.parse(sessionFile.path)
            val tokenUsage = tokenUsageExtractor.extract(parseResult.events)
            val matchedSkillName = skillUsageMatcher.matchSkill(parseResult.events, skillNames)
            val efficiencySummary = efficiencySummaryCalculator.calculate(
                events = parseResult.events,
                tokenUsage = tokenUsage,
                sizeBytes = sessionFile.sizeBytes,
                settings = normalizedSettings,
            )

            SkillRunInsight(
                sessionPath = sessionFile.path,
                sessionFileName = sessionFile.fileName,
                lastModifiedMs = sessionFile.lastModifiedMs,
                sizeBytes = sessionFile.sizeBytes,
                matchedSkillName = matchedSkillName,
                tokenUsage = tokenUsage,
                efficiencySummary = efficiencySummary,
                warnings = parseResult.warnings + efficiencySummary.warnings,
            )
        }

        return SkillOpsRunInsightsReport(
            insights = insights,
            warnings = warnings,
        )
    }
}
