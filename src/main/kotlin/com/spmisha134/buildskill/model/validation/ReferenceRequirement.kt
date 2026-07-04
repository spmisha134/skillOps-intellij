package com.spmisha134.buildskill.model.validation

data class ReferenceRequirement(
    val relativePath: String,
    val invalidMessage: String,
    val isValid: (String) -> Boolean,
)
