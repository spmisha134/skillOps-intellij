package com.spmisha134.skillops.presentation

import com.spmisha134.skillops.generator.SkillGenerationException

class CreateSkillExceptionHandler {
    fun userMessage(error: Throwable): String =
        error.message
            ?.takeIf(String::isNotBlank)
            ?: fallbackMessage(error)

    private fun fallbackMessage(error: Throwable): String =
        listOf(
            "Invalid skill generation request." to (error is SkillGenerationException || error is IllegalArgumentException),
            "Unknown error." to true,
        ).first { (_, matches) -> matches }.first
}
