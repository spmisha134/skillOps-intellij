package com.spmisha134.skillops.insights.run

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.spmisha134.skillops.insights.parser.CodexRawEvent
import java.nio.file.InvalidPathException
import java.nio.file.Path

class ProjectSessionMatcher {
    fun belongsToProject(events: List<CodexRawEvent>, projectRoot: Path): Boolean? {
        val recordedPaths = events.flatMap(::projectPaths)
        if (recordedPaths.isEmpty()) {
            return null
        }

        val normalizedProjectRoot = projectRoot.toAbsolutePath().normalize()
        return recordedPaths.any { recordedPath ->
            normalize(recordedPath)?.startsWith(normalizedProjectRoot) == true
        }
    }

    private fun projectPaths(event: CodexRawEvent): List<String> {
        val payload = event.payload?.getAsJsonObject("payload") ?: return emptyList()
        return buildList {
            payload.string("cwd")?.let(::add)
            payload.getAsJsonArray("workspace_roots")
                ?.mapNotNull { it.asStringOrNull() }
                ?.let(::addAll)

            payload.getAsJsonObject("state")
                ?.getAsJsonObject("environments")
                ?.getAsJsonObject("environments")
                ?.entrySet()
                ?.mapNotNull { (_, environment) -> environment.asJsonObjectOrNull()?.string("cwd") }
                ?.let(::addAll)
        }
    }

    private fun normalize(path: String): Path? =
        try {
            Path.of(path).toAbsolutePath().normalize()
        } catch (_: InvalidPathException) {
            null
        }

    private fun JsonObject.string(name: String): String? =
        get(name)?.asStringOrNull()

    private fun JsonElement.asStringOrNull(): String? =
        takeIf { isJsonPrimitive && asJsonPrimitive.isString }?.asString

    private fun JsonElement.asJsonObjectOrNull(): JsonObject? =
        takeIf(JsonElement::isJsonObject)?.asJsonObject
}
