package com.spmisha134.skillops.insights.codex

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.spmisha134.skillops.insights.parser.RawInsightEvent
import com.spmisha134.skillops.insights.parser.doubleAt
import com.spmisha134.skillops.insights.parser.longAt
import com.spmisha134.skillops.insights.parser.objectAt
import com.spmisha134.skillops.insights.parser.stringAt
import com.spmisha134.skillops.insights.usage.TokenUsage

class CodexUsageExtractor {
    fun extract(events: List<RawInsightEvent>): TokenUsage? {
        val event = events.asReversed().firstOrNull(::isTokenUsageEvent) ?: return null
        val payload = event.payload ?: return null
        val candidates = tokenUsageCandidates(payload)
        val cumulative = findNamedObject(payload, "total_token_usage", "totalTokenUsage")
        val lastTurn = findNamedObject(payload, "last_token_usage", "lastTokenUsage")

        val inputTokens = cumulative?.let { listOf(it).findLong(INPUT_TOKEN_KEYS) } ?: candidates.findLong(INPUT_TOKEN_KEYS)
        val outputTokens = cumulative?.let { listOf(it).findLong(OUTPUT_TOKEN_KEYS) } ?: candidates.findLong(OUTPUT_TOKEN_KEYS)
        val totalTokens = cumulative?.let { listOf(it).findLong(TOTAL_TOKEN_KEYS) } ?: candidates.findLong(TOTAL_TOKEN_KEYS)
            ?: if (inputTokens != null && outputTokens != null) inputTokens + outputTokens else null
        val cachedInputTokens = cumulative?.let { listOf(it).findLong(CACHED_INPUT_TOKEN_KEYS) }
            ?: candidates.findLong(CACHED_INPUT_TOKEN_KEYS)
        val reasoningOutputTokens = cumulative?.let { listOf(it).findLong(REASONING_OUTPUT_TOKEN_KEYS) }
            ?: candidates.findLong(REASONING_OUTPUT_TOKEN_KEYS)

        return TokenUsage(
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            cachedInputTokens = cachedInputTokens,
            reasoningOutputTokens = reasoningOutputTokens,
            totalTokens = totalTokens,
            rateLimitUsedPercent = candidates.findDouble(RATE_LIMIT_USED_PERCENT_KEYS),
            rateLimitResetAt = candidates.findString(RATE_LIMIT_RESET_AT_KEYS),
            rawEvidence = event.rawEvidence(payload),
            cacheCreationInputTokens = candidates.findLong(CACHE_CREATION_INPUT_TOKEN_KEYS),
            toolTokens = null,
            lastTurnInputTokens = lastTurn?.let { listOf(it).findLong(INPUT_TOKEN_KEYS) },
            lastTurnOutputTokens = lastTurn?.let { listOf(it).findLong(OUTPUT_TOKEN_KEYS) },
            lastTurnCachedInputTokens = lastTurn?.let { listOf(it).findLong(CACHED_INPUT_TOKEN_KEYS) },
            lastTurnReasoningOutputTokens = lastTurn?.let { listOf(it).findLong(REASONING_OUTPUT_TOKEN_KEYS) },
            lastTurnTotalTokens = lastTurn?.let { listOf(it).findLong(TOTAL_TOKEN_KEYS) },
            contextWindowTokens = candidates.findLong(CONTEXT_WINDOW_KEYS),
            modelName = findStringDeep(payload, "model", "model_name", "modelName"),
            durationMs = findLongDeep(payload, "duration_ms", "durationMs"),
            turnCount = findLongDeep(payload, "num_turns", "numTurns")?.toInt(),
        )
    }

    private fun isTokenUsageEvent(event: RawInsightEvent): Boolean {
        val payload = event.payload ?: return false
        return event.type == TOKEN_COUNT_EVENT_TYPE ||
            payload.objectAt("payload")?.stringAt("type") == TOKEN_COUNT_EVENT_TYPE ||
            payload.containsAny(TOKEN_USAGE_OBJECT_KEYS) ||
            payload.objectAt("payload")?.containsAny(TOKEN_USAGE_OBJECT_KEYS) == true
    }

    private fun tokenUsageCandidates(payload: JsonObject): List<JsonObject> {
        val candidates = mutableListOf<JsonObject>()
        candidates += payload
        payload.objectAt("payload")?.let(candidates::add)

        var index = 0
        while (index < candidates.size) {
            val candidate = candidates[index++]
            TOKEN_USAGE_OBJECT_KEYS
                .mapNotNull { key -> candidate.objectAt(key) }
                .filterNot(candidates::contains)
                .forEach(candidates::add)
        }

        val rateLimits = candidates.flatMap { candidate ->
            listOfNotNull(candidate.objectAt("rate_limits"), candidate.objectAt("rateLimits"))
        }
        candidates += rateLimits

        return candidates.distinct()
    }

    private fun JsonObject.containsAny(keys: Set<String>): Boolean =
        keys.any(::has)

    private fun List<JsonObject>.findLong(keys: List<String>): Long? =
        firstNotNullOfOrNull { candidate -> keys.firstNotNullOfOrNull { key -> candidate.longAt(key) } }

    private fun List<JsonObject>.findDouble(keys: List<String>): Double? =
        firstNotNullOfOrNull { candidate -> keys.firstNotNullOfOrNull { key -> candidate.doubleAt(key) } }

    private fun List<JsonObject>.findString(keys: List<String>): String? =
        firstNotNullOfOrNull { candidate -> keys.firstNotNullOfOrNull { key -> candidate.stringAt(key) } }

    private fun findNamedObject(root: JsonObject, vararg names: String): JsonObject? {
        root.entrySet().forEach { (key, value) ->
            if (key in names && value.isJsonObject) return value.asJsonObject
            if (value.isJsonObject) findNamedObject(value.asJsonObject, *names)?.let { return it }
            if (value.isJsonArray) value.asJsonArray.forEach { child ->
                if (child.isJsonObject) findNamedObject(child.asJsonObject, *names)?.let { return it }
            }
        }
        return null
    }

    private fun findStringDeep(root: JsonObject, vararg names: String): String? =
        findValueDeep(root, names)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString

    private fun findLongDeep(root: JsonObject, vararg names: String): Long? =
        findValueDeep(root, names)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asLong

    private fun findDoubleDeep(root: JsonObject, vararg names: String): Double? =
        findValueDeep(root, names)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asDouble

    private fun findValueDeep(root: JsonObject, names: Array<out String>): JsonElement? {
        root.entrySet().forEach { (key, value) ->
            if (key in names) return value
            if (value.isJsonObject) findValueDeep(value.asJsonObject, names)?.let { return it }
            if (value.isJsonArray) value.asJsonArray.forEach { child ->
                if (child.isJsonObject) findValueDeep(child.asJsonObject, names)?.let { return it }
            }
        }
        return null
    }

    private fun RawInsightEvent.rawEvidence(payload: JsonObject): Map<String, Any?> =
        mapOf(
            "lineNumber" to lineNumber,
            "timestamp" to timestamp,
            "type" to type,
            "payload" to payload.toPlainValue(),
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
            "info",
            "total_token_usage",
            "totalTokenUsage",
            "last_token_usage",
            "lastTokenUsage",
            "rate_limits",
            "rateLimits",
            "primary",
            "secondary",
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

        private val CACHE_CREATION_INPUT_TOKEN_KEYS = listOf(
            "cache_creation_input_tokens",
            "cacheCreationInputTokens",
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

        private val CONTEXT_WINDOW_KEYS = listOf(
            "model_context_window",
            "modelContextWindow",
        )
    }
}
