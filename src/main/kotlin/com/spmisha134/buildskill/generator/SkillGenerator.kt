package com.spmisha134.buildskill.generator

import com.spmisha134.buildskill.generator.openai.OpenAiYamlRenderer
import com.spmisha134.buildskill.model.generation.SkillGenerationRequest
import com.spmisha134.buildskill.model.generation.SkillGenerationResult
import com.spmisha134.buildskill.model.validation.SkillValidationPaths
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class SkillGenerator(
    private val pathResolver: SkillPathResolver = SkillPathResolver(),
    private val skillTemplateRenderer: SkillTemplateRenderer = SkillTemplateRenderer(),
    private val referenceTemplateRenderer: ReferenceTemplateRenderer = ReferenceTemplateRenderer(),
    private val openAiYamlRenderer: OpenAiYamlRenderer = OpenAiYamlRenderer()
) {
    fun generate(request: SkillGenerationRequest): SkillGenerationResult {
        val definition = request.skillDefinition
        val normalizedName = SkillNameNormalizer.normalize(definition.name)
        val skillDirectory = pathResolver.resolveSkillDirectory(request.projectBasePath, normalizedName)

        if (Files.exists(skillDirectory)) {
            throw SkillGenerationException("A skill folder with this name already exists.")
        }

        val createdFiles = mutableListOf<Path>()
        val createdDirectories = mutableListOf<Path>()

        createDirectory(skillDirectory, createdDirectories)
        val referencesDirectory = createDirectory(skillDirectory.resolve(SkillValidationPaths.REFERENCES_DIRECTORY), createdDirectories)

        writeFile(skillDirectory.resolve(SkillValidationPaths.SKILL_MD), skillTemplateRenderer.render(definition), createdFiles)
        writeFile(referencesDirectory.resolve(SkillValidationPaths.INSTRUCTIONS_REFERENCE_FILE), referenceTemplateRenderer.renderInstructions(definition), createdFiles)
        writeFile(referencesDirectory.resolve(SkillValidationPaths.VALIDATION_REFERENCE_FILE), referenceTemplateRenderer.renderValidation(definition), createdFiles)
        writeFile(referencesDirectory.resolve(SkillValidationPaths.EXAMPLES_REFERENCE_FILE), referenceTemplateRenderer.renderExamples(definition), createdFiles)

        if (definition.createScriptsDirectory) {
            createDirectory(skillDirectory.resolve(SkillValidationPaths.SCRIPTS_DIRECTORY), createdDirectories)
        }
        if (definition.createAssetsDirectory) {
            createDirectory(skillDirectory.resolve(SkillValidationPaths.ASSETS_DIRECTORY), createdDirectories)
        }
        val agentsDirectory = createDirectory(skillDirectory.resolve(SkillValidationPaths.AGENT_DIRECTORY), createdDirectories)
        writeFile(agentsDirectory.resolve(SkillValidationPaths.OPENAI_YAML_FILE), openAiYamlRenderer.render(definition), createdFiles)

        val skillMdPath = skillDirectory.resolve(SkillValidationPaths.SKILL_MD)
        return SkillGenerationResult(
            skillDirectoryPath = skillDirectory.toString(),
            skillMdPath = skillMdPath.toString(),
            createdFiles = createdFiles.map { it.toString() },
            createdDirectories = createdDirectories.map { it.toString() }
        )
    }

    private fun createDirectory(path: Path, createdDirectories: MutableList<Path>): Path {
        path.createDirectories()
        createdDirectories.add(path)
        return path
    }

    private fun writeFile(path: Path, content: String, createdFiles: MutableList<Path>) {
        if (Files.exists(path)) {
            throw SkillGenerationException("Refusing to overwrite existing file: $path")
        }
        path.writeText(content)
        createdFiles.add(path)
    }
}

class SkillGenerationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
