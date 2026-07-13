package com.spmisha134.skillops.insights.run

import com.spmisha134.skillops.insights.usage.TokenUsage
import java.nio.file.Path

data class SkillRunInsight(
    val sessionPath: Path,
    val sessionFileName: String,
    val lastModifiedMs: Long,
    val sizeBytes: Long,
    val matchedSkillName: String?,
    val tokenUsage: TokenUsage?,
    val efficiencySummary: EfficiencySummary,
    val warnings: List<String>,
)
