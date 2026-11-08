package com.spmisha134.skillops.insights.usage

data class RateLimitWindow(
    val usedPercent: Double?,
    val windowMinutes: Long?,
    val resetsAtEpochSeconds: Long?,
)
