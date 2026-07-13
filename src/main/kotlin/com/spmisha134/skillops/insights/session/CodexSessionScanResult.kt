package com.spmisha134.skillops.insights.session

data class CodexSessionScanResult(
    val files: List<CodexSessionFile>,
    val warnings: List<String>,
)
