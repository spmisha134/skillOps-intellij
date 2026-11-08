package com.spmisha134.skillops.sessions.terminal

object ClaudeResumeCommand {
    fun build(sessionId: String, modelName: String? = null): String =
        "claude${modelName?.let { " --model ${shellQuote(it)}" }.orEmpty()} --resume $sessionId"
    fun continueMostRecent(): String = "claude --continue"
    fun chooseSession(): String = "claude --resume"
    fun resumeNamed(name: String): String = "claude -n ${shellQuote(name)}"

    private fun shellQuote(value: String): String = "'${value.replace("'", "'\\''")}'"
}
