package com.spmisha134.skillops.insights.run

import com.spmisha134.skillops.insights.parser.CodexRawEvent

class SkillUsageMatcher {
    fun matchSkill(events: List<CodexRawEvent>, skillNames: List<String>): String? {
        if (events.isEmpty() || skillNames.isEmpty()) {
            return null
        }

        val searchableText = events.joinToString(separator = "\n") { it.rawText.lowercase() }
        return skillNames.firstOrNull { skillName ->
            val normalizedSkillName = skillName.lowercase()
            searchableText.contains(".agents/skills/$normalizedSkillName") ||
                searchableText.contains("agents/skills/$normalizedSkillName") ||
                searchableText.contains("skill: $normalizedSkillName") ||
                searchableText.contains("name: $normalizedSkillName") ||
                searchableText.contains("`$normalizedSkillName`") ||
                searchableText.contains("\"$normalizedSkillName\"")
        }
    }
}
