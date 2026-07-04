package com.spmisha134.buildskill.generator

object SkillNameNormalizer {
    private val whitespace = Regex("\\s+")
    private val unsupported = Regex("[^a-z0-9-]")
    private val repeatedHyphen = Regex("-+")

    fun normalize(input: String): String {
        val normalized = input
            .trim()
            .lowercase()
            .replace(whitespace, "-")
            .replace(unsupported, "")
            .replace(repeatedHyphen, "-")
            .trim('-')

        require(normalized.isNotBlank()) { "Skill name must contain at least one supported character." }
        return normalized
    }
}
