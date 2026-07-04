package com.spmisha134.buildskill.generator.openai

object YamlScalarFormatter {
    fun quoted(value: String): String =
        "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"") + "\""
}
