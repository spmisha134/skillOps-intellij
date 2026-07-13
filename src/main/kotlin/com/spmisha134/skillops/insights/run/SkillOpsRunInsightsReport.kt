package com.spmisha134.skillops.insights.run

data class SkillOpsRunInsightsReport(
    val insights: List<SkillRunInsight>,
    val warnings: List<String>,
) {
    val latestInsight: SkillRunInsight?
        get() = insights.firstOrNull()
}
