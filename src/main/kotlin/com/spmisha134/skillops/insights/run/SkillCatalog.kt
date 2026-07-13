package com.spmisha134.skillops.insights.run

import java.nio.file.Files
import java.nio.file.Path

class SkillCatalog {
    fun discover(projectRoot: Path): List<String> {
        val skillsRoot = projectRoot.resolve(".agents").resolve("skills")
        if (!Files.isDirectory(skillsRoot)) {
            return emptyList()
        }

        return Files.newDirectoryStream(skillsRoot).use { entries ->
            entries
                .filter { Files.isDirectory(it) }
                .map { it.fileName.toString() }
                .sorted()
                .toList()
        }
    }
}
