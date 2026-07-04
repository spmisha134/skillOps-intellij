package com.spmisha134.buildskill.validator.rules.openai

import com.spmisha134.buildskill.model.validation.RequiredYamlContent
import com.spmisha134.buildskill.model.validation.SkillValidationIssue
import com.spmisha134.buildskill.model.validation.SkillValidationPaths
import com.spmisha134.buildskill.validator.SkillRule
import com.spmisha134.buildskill.model.validation.SkillValidationContext
import com.spmisha134.buildskill.validator.rules.common.error
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.readText

class OpenAiYamlRule : SkillRule {
    override fun validate(context: SkillValidationContext): List<SkillValidationIssue> {
        val path = context.skillDirectoryPath.resolve(SkillValidationPaths.OPENAI_YAML)
        return path.validationBarrier()
            ?: validateContent(path.readText(), context.skillDirectoryPath.name, path)
    }

    private fun java.nio.file.Path.validationBarrier(): List<SkillValidationIssue>? =
        listOfNotNull(
            takeUnless(Files::exists)?.let { error("${SkillValidationPaths.OPENAI_YAML} is required.", it) },
            takeIf(Files::exists)?.takeUnless(Files::isRegularFile)
                ?.let { error("${SkillValidationPaths.OPENAI_YAML} must be a file.", it) },
        ).takeIf(List<SkillValidationIssue>::isNotEmpty)

    private fun validateContent(
        content: String,
        skillName: String,
        path: java.nio.file.Path,
    ): List<SkillValidationIssue> =
        listOf(
            RequiredYamlContent("${SkillValidationPaths.OPENAI_YAML} must contain interface:.") {
                content.lineSequence().any { line -> line.trim() == "interface:" }
            },
            RequiredYamlContent("${SkillValidationPaths.OPENAI_YAML} must contain display_name:.") {
                content.contains("display_name:")
            },
            RequiredYamlContent("${SkillValidationPaths.OPENAI_YAML} must contain short_description:.") {
                content.contains("short_description:")
            },
            RequiredYamlContent("${SkillValidationPaths.OPENAI_YAML} must contain default_prompt:.") {
                content.contains("default_prompt:")
            },
            RequiredYamlContent("${SkillValidationPaths.OPENAI_YAML} default_prompt must reference \$$skillName.") {
                content.contains("\$$skillName")
            },
        ).mapNotNull { requirement ->
            requirement.takeUnless { it.isSatisfied() }?.let { error(it.message, path) }
        }

}
