package com.spmisha134.skillops.insights.parser

import java.nio.file.Path

data class CodexParseResult(
    val filePath: Path,
    val events: List<CodexRawEvent>,
    val warnings: List<String>,
)
