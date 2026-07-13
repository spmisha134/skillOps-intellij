package com.spmisha134.skillops.insights.run

data class EfficiencySummary(
    val outputInputRatio: Double?,
    val cachedInputPercent: Double?,
    val reasoningOutputPercent: Double?,
    val searchCount: Int,
    val warnings: List<String>,
)
