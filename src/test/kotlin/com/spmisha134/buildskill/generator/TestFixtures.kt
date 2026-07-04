package com.spmisha134.buildskill.generator

import com.spmisha134.buildskill.model.skill.SkillDefinition

fun testDefinition(
    name: String = "Test Skill",
    description: String = "Use for tests",
    createScriptsDirectory: Boolean = true,
    createAssetsDirectory: Boolean = true,
) = SkillDefinition(
    name = name,
    description = description,
    createScriptsDirectory = createScriptsDirectory,
    createAssetsDirectory = createAssetsDirectory,
)
