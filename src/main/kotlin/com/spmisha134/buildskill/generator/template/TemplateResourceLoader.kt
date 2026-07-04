package com.spmisha134.buildskill.generator.template

class TemplateResourceLoader(
    private val classLoader: ClassLoader = TemplateResourceLoader::class.java.classLoader
) {
    fun load(resourcePath: String): String {
        return classLoader.getResourceAsStream(resourcePath)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("Template resource not found: $resourcePath")
    }
}
