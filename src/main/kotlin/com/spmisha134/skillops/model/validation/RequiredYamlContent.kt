package com.spmisha134.skillops.model.validation

data class RequiredYamlContent(
    val message: String,
    val isSatisfied: () -> Boolean,
)
