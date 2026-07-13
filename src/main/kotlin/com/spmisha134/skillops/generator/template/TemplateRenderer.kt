package com.spmisha134.skillops.generator.template

class TemplateRenderer(
    private val templateResourceLoader: TemplateResourceLoader = TemplateResourceLoader()
) {
    fun render(resourcePath: String, variables: Map<String, String> = emptyMap()): String {
        return variables.entries.fold(templateResourceLoader.load(resourcePath)) { content, (name, value) ->
            content.replace("{{$name}}", value)
        }
    }
}
