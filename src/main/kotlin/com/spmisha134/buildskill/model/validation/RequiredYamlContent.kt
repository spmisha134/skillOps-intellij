package com.spmisha134.buildskill.model.validation

data class RequiredYamlContent(
    val message: String,
    val isSatisfied: () -> Boolean,
)
