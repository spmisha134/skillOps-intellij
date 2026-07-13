package com.spmisha134.skillops.validator.rules.references

import com.spmisha134.skillops.model.validation.ReferenceRequirement
import com.spmisha134.skillops.model.validation.SkillValidationIssue
import com.spmisha134.skillops.model.validation.SkillValidationPaths
import com.spmisha134.skillops.validator.SkillRule
import com.spmisha134.skillops.model.validation.SkillValidationContext
import com.spmisha134.skillops.validator.rules.common.error
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

class RequiredReferencesRule : SkillRule {
    private val requiredReferences = listOf(
        ReferenceRequirement(
            relativePath = SkillValidationPaths.INSTRUCTIONS_REFERENCE,
            invalidMessage = "${SkillValidationPaths.INSTRUCTIONS_REFERENCE} must not be blank.",
            isValid = String::isNotBlank,
        ),
        ReferenceRequirement(
            relativePath = SkillValidationPaths.VALIDATION_REFERENCE,
            invalidMessage = "${SkillValidationPaths.VALIDATION_REFERENCE} must contain checklist items.",
            isValid = { content -> content.contains("- [ ]") },
        ),
        ReferenceRequirement(
            relativePath = SkillValidationPaths.EXAMPLES_REFERENCE,
            invalidMessage = "${SkillValidationPaths.EXAMPLES_REFERENCE} must contain good and bad example sections.",
            isValid = { content ->
                content.lowercase().let { it.contains("## good example") && it.contains("## bad example") }
            },
        ),
    )

    override fun validate(context: SkillValidationContext): List<SkillValidationIssue> {
        return requiredReferences.flatMap { requirement ->
            validateReference(context.skillDirectoryPath.resolve(requirement.relativePath), requirement)
        }
    }

    private fun validateReference(
        path: Path,
        requirement: ReferenceRequirement,
    ): List<SkillValidationIssue> =
        path.validationBarrier(requirement.relativePath)
            ?: requirement.toIssue(path.readText(), path)

    private fun ReferenceRequirement.toIssue(content: String, path: Path): List<SkillValidationIssue> =
        content
            .takeUnless(isValid)
            ?.let { listOf(error(invalidMessage, path)) }
            ?: emptyList()

    private fun Path.validationBarrier(relativePath: String): List<SkillValidationIssue>? =
        listOfNotNull(
            takeUnless(Files::isRegularFile)?.let { error("$relativePath is required.", it) },
            takeIf(Files::isRegularFile)
                ?.takeIf { Files.size(it) == 0L }
                ?.let { error("$relativePath must not be blank.", it) },
        ).takeIf(List<SkillValidationIssue>::isNotEmpty)

}
