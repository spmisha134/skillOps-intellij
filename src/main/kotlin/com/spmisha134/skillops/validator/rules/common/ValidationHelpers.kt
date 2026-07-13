package com.spmisha134.skillops.validator.rules.common

import com.spmisha134.skillops.model.validation.SkillValidationIssue
import com.spmisha134.skillops.model.validation.SkillValidationPaths
import com.spmisha134.skillops.model.validation.SkillValidationSeverity
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

internal fun error(message: String, filePath: Path? = null) = SkillValidationIssue(
    severity = SkillValidationSeverity.ERROR,
    message = message,
    filePath = filePath?.toString()
)

internal fun warning(message: String, filePath: Path? = null) = SkillValidationIssue(
    severity = SkillValidationSeverity.WARNING,
    message = message,
    filePath = filePath?.toString()
)

internal fun skillMd(skillDirectoryPath: Path): Path = skillDirectoryPath.resolve(SkillValidationPaths.SKILL_MD)

internal fun readSkillMd(skillDirectoryPath: Path): String? {
    val path = skillMd(skillDirectoryPath)
    return if (Files.isRegularFile(path)) path.readText() else null
}

internal fun frontMatter(content: String): Map<String, String>? {
    val lines = content.lineSequence().toList()
    if (lines.firstOrNull() != "---") {
        return null
    }
    val closingIndex = lines.drop(1).indexOf("---")
    if (closingIndex < 0) {
        return null
    }
    return lines
        .drop(1)
        .take(closingIndex)
        .mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0) {
                null
            } else {
                line.substring(0, separator).trim() to line.substring(separator + 1).trim()
            }
        }
        .toMap()
}
