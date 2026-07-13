package com.spmisha134.skillops.validator.rules.content

import com.spmisha134.skillops.validator.SkillRule
import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.validator.rules.common.error
import com.spmisha134.skillops.validator.rules.common.readSkillMd
import com.spmisha134.skillops.validator.rules.common.skillMd

class ProjectInstructionDiscoveryRule : SkillRule {
    override fun validate(context: SkillValidationContext): List<com.spmisha134.skillops.model.validation.SkillValidationIssue> {
        val content = readSkillMd(context.skillDirectoryPath) ?: return emptyList()
        val hasAgents = content.contains("AGENTS.md") && content.contains("agents.md")
        val hasClaude = content.contains("CLAUDE.md") && content.contains("claude.md")
        val hasGemini = content.contains("GEMINI.md") && content.contains("gemini.md")

        return if (hasAgents && hasClaude && hasGemini) {
            emptyList()
        } else {
            listOf(error("SKILL.md must require project instruction discovery for AGENTS.md, CLAUDE.md, and GEMINI.md variants.", skillMd(context.skillDirectoryPath)))
        }
    }
}
