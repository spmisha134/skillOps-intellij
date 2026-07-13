package com.spmisha134.skillops.generator.openai

object YamlScalarFormatter {
    fun quoted(value: String): String =
        "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"") + "\""
}
