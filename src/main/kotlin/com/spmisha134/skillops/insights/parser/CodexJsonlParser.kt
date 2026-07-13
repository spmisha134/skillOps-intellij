package com.spmisha134.skillops.insights.parser

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class CodexJsonlParser {
    fun parse(path: Path): CodexParseResult {
        val events = mutableListOf<CodexRawEvent>()
        val warnings = mutableListOf<String>()

        try {
            Files.newBufferedReader(path, StandardCharsets.UTF_8).use { reader ->
                reader.lineSequence().forEachIndexed { index, line ->
                    parseLine(lineNumber = index + 1, rawText = line, warnings = warnings)?.let(events::add)
                }
            }
        } catch (exception: IOException) {
            warnings += "Could not read JSONL file $path: ${exception.message ?: exception.javaClass.simpleName}"
        } catch (exception: SecurityException) {
            warnings += "Permission denied reading JSONL file $path: ${exception.message ?: exception.javaClass.simpleName}"
        }

        return CodexParseResult(
            filePath = path.toAbsolutePath().normalize(),
            events = events,
            warnings = warnings,
        )
    }

    private fun parseLine(
        lineNumber: Int,
        rawText: String,
        warnings: MutableList<String>,
    ): CodexRawEvent? {
        if (rawText.isBlank()) {
            return null
        }

        val parsed = try {
            JsonParser.parseString(rawText)
        } catch (exception: JsonSyntaxException) {
            val message = "Line $lineNumber: malformed JSON (${exception.message ?: exception.javaClass.simpleName})"
            warnings += message
            return CodexRawEvent(
                lineNumber = lineNumber,
                timestamp = null,
                type = null,
                payload = null,
                rawText = rawText,
                parseError = message,
            )
        }

        if (!parsed.isJsonObject) {
            val message = "Line $lineNumber: JSONL event is not an object"
            warnings += message
            return CodexRawEvent(
                lineNumber = lineNumber,
                timestamp = null,
                type = null,
                payload = null,
                rawText = rawText,
                parseError = message,
            )
        }

        val payload = parsed.asJsonObject
        return CodexRawEvent(
            lineNumber = lineNumber,
            timestamp = payload.stringAt("timestamp"),
            type = payload.eventType(),
            payload = payload,
            rawText = rawText,
            parseError = null,
        )
    }

    private fun JsonObject.eventType(): String? =
        stringAt("type")
            ?: stringAt("event")
            ?: objectAt("event")?.stringAt("type")
            ?: objectAt("message")?.stringAt("type")
            ?: stringAt("message")

    private fun JsonObject.objectAt(key: String): JsonObject? {
        val element = get(key) ?: return null
        return if (element.isJsonObject) element.asJsonObject else null
    }

    private fun JsonObject.stringAt(key: String): String? {
        val element = get(key) ?: return null
        return element.stringValue()
    }

    private fun JsonElement.stringValue(): String? =
        if (isJsonPrimitive && asJsonPrimitive.isString) {
            asString
        } else {
            null
        }
}
