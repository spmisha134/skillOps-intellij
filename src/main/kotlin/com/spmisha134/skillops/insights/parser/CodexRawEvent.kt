package com.spmisha134.skillops.insights.parser

import com.google.gson.JsonObject

data class CodexRawEvent(
    val lineNumber: Int,
    val timestamp: String?,
    val type: String?,
    val payload: JsonObject?,
    val rawText: String,
    val parseError: String?,
)
