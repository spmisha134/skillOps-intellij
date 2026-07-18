package com.spmisha134.skillops.model.skill

enum class SkillPlatform(
    val displayName: String,
    val projectDirectory: String,
) {
    CODEX("Codex", ".agents"),
    CLAUDE("Claude", ".claude"),
    GEMINI("Gemini", ".gemini"),
}
