package com.spmisha134.skillops.insights.usage

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.spmisha134.skillops.insights.parser.CodexRawEvent

class TokenUsageExtractor {
    fun extract(events: List<CodexRawEvent>): TokenUsage? {
        val event = events.asReversed().firstOrNull(::isTokenUsageEvent) ?: return null
        val payload = event.payload ?: return null
        val candidates = tokenUsageCandidates(payload)

        val inputTokens = candidates.findLong(INPUT_TOKEN_KEYS)
        val outputTokens = candidates.findLong(OUTPUT_TOKEN_KEYS)
        val totalTokens = candidates.findLong(TOTAL_TOKEN_KEYS)
            ?: if (inputTokens != null && outputTokens != null) inputTokens + outputTokens else null

        return TokenUsage(
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            cachedInputTokens = candidates.findLong(CACHED_INPUT_TOKEN_KEYS),
            reasoningOutputTokens = candidates.findLong(REASONING_OUTPUT_TOKEN_KEYS),
            totalTokens = totalTokens,
            rateLimitUsedPercent = candidates.findDouble(RATE_LIMIT_USED_PERCENT_KEYS),
            rateLimitResetAt = candidates.findString(RATE_LIMIT_RESET_AT_KEYS),
            rawEvidence = event.rawEvidence(payload),
        )
    }

    private fun isTokenUsageEvent(event: CodexRawEvent): Boolean {
        val payload = event.payload ?: return false
        return event.type == TOKEN_COUNT_EVENT_TYPE ||
            payload.containsAny(TOKEN_USAGE_OBJECT_KEYS) ||
            payload.objectAt("payload")?.containsAny(TOKEN_USAGE_OBJECT_KEYS) == true
    }

    private fun tokenUsageCandidates(payload: JsonObject): List<JsonObject> {
        val candidates = mutableListOf<JsonObject>()
        candidates += payload
        payload.objectAt("payload")?.let(candidates::add)

        val snapshot = candidates.toList()
        for (candidate in snapshot) {
            TOKEN_USAGE_OBJECT_KEYS.mapNotNull { key -> candidate.objectAt(key) }.forEach(candidates::add)
        }

        val rateLimits = candidates.flatMap { candidate ->
            listOfNotNull(candidate.objectAt("rate_limits"), candidate.objectAt("rateLimits"))
        }
        candidates += rateLimits

        return candidates.distinct()
    }

    private fun JsonObject.containsAny(keys: Set<String>): Boolean =
        keys.any(::has)

    private fun JsonObject.objectAt(key: String): JsonObject? {
        val element = get(key) ?: return null
        return if (element.isJsonObject) element.asJsonObject else null
    }

    private fun List<JsonObject>.findLong(keys: List<String>): Long? =
        firstNotNullOfOrNull { candidate -> keys.firstNotNullOfOrNull { key -> candidate.longAt(key) } }

    private fun List<JsonObject>.findDouble(keys: List<String>): Double? =
        firstNotNullOfOrNull { candidate -> keys.firstNotNullOfOrNull { key -> candidate.doubleAt(key) } }

    private fun List<JsonObject>.findString(keys: List<String>): String? =
        firstNotNullOfOrNull { candidate -> keys.firstNotNullOfOrNull { key -> candidate.stringAt(key) } }

    private fun JsonObject.longAt(key: String): Long? {
        val element = get(key) ?: return null
        return element.longValue()
    }

    private fun JsonObject.doubleAt(key: String): Double? {
        val element = get(key) ?: return null
        return element.doubleValue()
    }

    private fun JsonObject.stringAt(key: String): String? {
        val element = get(key) ?: return null
        return element.stringValue()
    }

    private fun JsonElement.longValue(): Long? {
        if (!isJsonPrimitive) {
            return null
        }
        val primitive = asJsonPrimitive
        return when {
            primitive.isNumber -> runCatching { primitive.asLong }.getOrNull()
            primitive.isString -> primitive.asString.toLongOrNull()
            else -> null
        }
    }

    private fun JsonElement.doubleValue(): Double? {
        if (!isJsonPrimitive) {
            return null
        }
        val primitive = asJsonPrimitive
        return when {
            primitive.isNumber -> runCatching { primitive.asDouble }.getOrNull()
            primitive.isString -> primitive.asString.toDoubleOrNull()
            else -> null
        }
    }

    private fun JsonElement.stringValue(): String? =
        if (isJsonPrimitive && asJsonPrimitive.isString) {
            asString
        } else {
            null
        }

    private fun CodexRawEvent.rawEvidence(payload: JsonObject): Map<String, Any?> =
        mapOf(
            "lineNumber" to lineNumber,
            "timestamp" to timestamp,
            "type" to type,
            "payload" to payload.toPlainValue(),
            "rawText" to rawText,
        )

    private fun JsonElement.toPlainValue(): Any? =
        when (this) {
            is JsonNull -> null
            is JsonObject -> entrySet().associate { (key, value) -> key to value.toPlainValue() }
            is JsonArray -> map { it.toPlainValue() }
            is JsonPrimitive -> toPrimitiveValue()
            else -> toString()
        }

    private fun JsonPrimitive.toPrimitiveValue(): Any? =
        when {
            isBoolean -> asBoolean
            isNumber -> asNumber
            isString -> asString
            else -> null
        }

    companion object {
        private const val TOKEN_COUNT_EVENT_TYPE = "token_count"

        private val TOKEN_USAGE_OBJECT_KEYS = setOf(
            "token_count",
            "tokenCount",
            "usage",
            "rate_limits",
            "rateLimits",
        )

        private val INPUT_TOKEN_KEYS = listOf(
            "input_tokens",
            "inputTokens",
            "prompt_tokens",
            "promptTokens",
        )

        private val OUTPUT_TOKEN_KEYS = listOf(
            "output_tokens",
            "outputTokens",
            "completion_tokens",
            "completionTokens",
        )

        private val CACHED_INPUT_TOKEN_KEYS = listOf(
            "cached_input_tokens",
            "cachedInputTokens",
            "cache_read_input_tokens",
            "cacheReadInputTokens",
        )

        private val REASONING_OUTPUT_TOKEN_KEYS = listOf(
            "reasoning_output_tokens",
            "reasoningOutputTokens",
            "reasoning_tokens",
            "reasoningTokens",
        )

        private val TOTAL_TOKEN_KEYS = listOf(
            "total_tokens",
            "totalTokens",
            "total_token_count",
            "totalTokenCount",
        )

        private val RATE_LIMIT_USED_PERCENT_KEYS = listOf(
            "rate_limit_used_percent",
            "rateLimitUsedPercent",
            "used_percent",
            "usedPercent",
            "percent_used",
            "percentUsed",
        )

        private val RATE_LIMIT_RESET_AT_KEYS = listOf(
            "rate_limit_reset_at",
            "rateLimitResetAt",
            "reset_at",
            "resetAt",
        )
    }
}
