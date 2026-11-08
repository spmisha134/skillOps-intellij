package com.spmisha134.skillops.sessions.terminal

object CodexResumeCommand {
    fun build(sessionId: String, modelName: String? = null): String =
        "codex resume${modelName?.let { " --model ${shellQuote(it)}" }.orEmpty()} $sessionId"

    private fun shellQuote(value: String): String = "'${value.replace("'", "'\\''")}'"
}
