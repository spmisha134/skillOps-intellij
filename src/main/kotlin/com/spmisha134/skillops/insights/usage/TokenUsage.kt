package com.spmisha134.skillops.insights.usage

data class TokenUsage(
    val inputTokens: Long?,
    val outputTokens: Long?,
    val cachedInputTokens: Long?,
    val reasoningOutputTokens: Long?,
    val totalTokens: Long?,
    val rateLimitUsedPercent: Double?,
    val rateLimitResetAt: String?,
    val rawEvidence: Map<String, Any?>,
)
