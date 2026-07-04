package com.spmisha134.buildskill.model.validation

object SkillValidationPaths {
    const val AGENTS_DIRECTORY = ".agents"
    const val SKILLS_DIRECTORY = "skills"
    const val SKILL_MD = "SKILL.md"

    const val REFERENCES_DIRECTORY = "references"
    const val INSTRUCTIONS_REFERENCE_FILE = "instructions.md"
    const val VALIDATION_REFERENCE_FILE = "validation.md"
    const val EXAMPLES_REFERENCE_FILE = "examples.md"
    const val INSTRUCTIONS_REFERENCE = "$REFERENCES_DIRECTORY/instructions.md"
    const val VALIDATION_REFERENCE = "$REFERENCES_DIRECTORY/validation.md"
    const val EXAMPLES_REFERENCE = "$REFERENCES_DIRECTORY/examples.md"

    const val SCRIPTS_DIRECTORY = "scripts"
    const val ASSETS_DIRECTORY = "assets"
    const val AGENT_DIRECTORY = "agents"
    const val OPENAI_YAML_FILE = "openai.yaml"
    const val OPENAI_YAML = "$AGENT_DIRECTORY/$OPENAI_YAML_FILE"
}
