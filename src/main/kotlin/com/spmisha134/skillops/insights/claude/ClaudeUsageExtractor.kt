package com.spmisha134.skillops.insights.claude

import com.google.gson.JsonObject
import com.google.gson.JsonElement
import com.spmisha134.skillops.insights.parser.RawInsightEvent
import com.spmisha134.skillops.insights.parser.longAt
import com.spmisha134.skillops.insights.parser.objectAt
import com.spmisha134.skillops.insights.parser.stringAt
import com.spmisha134.skillops.insights.usage.TokenUsage

class ClaudeUsageExtractor {
    fun extract(events: List<RawInsightEvent>): TokenUsage? {
        val calls = events.mapNotNull(::assistantCall)
            .distinctBy { it.messageId }
        if (calls.isEmpty()) return null

        val input = calls.sumOf { it.usage.long("input_tokens") }
        val output = calls.sumOf { it.usage.long("output_tokens") }
        val cacheRead = calls.sumOf { it.usage.long("cache_read_input_tokens") }
        val cacheCreation = calls.sumOf { it.usage.long("cache_creation_input_tokens") }
        val latest = calls.last()
        val latestTotal = latest.usage.long("input_tokens") + latest.usage.long("output_tokens") +
            latest.usage.long("cache_read_input_tokens") + latest.usage.long("cache_creation_input_tokens")
        val serverSearches = calls.sumOf { it.usage.objectAt("server_tool_use")?.long("web_search_requests") ?: 0L }
        val serverFetches = calls.sumOf { it.usage.objectAt("server_tool_use")?.long("web_fetch_requests") ?: 0L }
        return TokenUsage(
            inputTokens = input,
            outputTokens = output,
            cachedInputTokens = cacheRead,
            reasoningOutputTokens = null,
            totalTokens = input + output + cacheRead + cacheCreation,
            rateLimitUsedPercent = null,
            rateLimitResetAt = null,
            rawEvidence = mapOf(
                "distinctAssistantCalls" to calls.size,
                "definition" to "input + output + cache read + cache creation",
            ),
            cacheCreationInputTokens = cacheCreation,
            toolTokens = null,
            lastTurnInputTokens = latest.usage.long("input_tokens"),
            lastTurnOutputTokens = latest.usage.long("output_tokens"),
            lastTurnCachedInputTokens = latest.usage.long("cache_read_input_tokens"),
            lastTurnTotalTokens = latestTotal,
            modelName = latest.model,
            turnCount = calls.size,
            durationMs = findLongDeep(events, "duration_ms", "durationMs"),
            serverWebSearchRequests = serverSearches.toInt().takeIf { it > 0 },
            serverWebFetchRequests = serverFetches.toInt().takeIf { it > 0 },
        )
    }

    private fun assistantCall(event: RawInsightEvent): AssistantCall? {
        val payload = event.payload ?: return null
        if (payload.stringAt("type") != "assistant") return null
        val message = payload.objectAt("message") ?: return null
        val messageId = message.stringAt("id") ?: return null
        val usage = message.objectAt("usage") ?: return null
        return AssistantCall(messageId, usage, message.stringAt("model"))
    }

    private fun JsonObject.long(key: String): Long = longAt(key) ?: 0L

    private fun findLongDeep(events: List<RawInsightEvent>, vararg names: String): Long? =
        events.firstNotNullOfOrNull { event -> event.payload?.let { findValueDeep(it, names) } }
            ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asLong

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

    private data class AssistantCall(val messageId: String, val usage: JsonObject, val model: String?)
}
