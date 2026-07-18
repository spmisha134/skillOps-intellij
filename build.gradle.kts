import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

val pluginRepositoryUrl: String by project

dependencies {
    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2025.2.6.2")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.spmisha134.skillops"
        name = "SkillOps"
        description = """
            <p>Create repository-scoped skills for Codex, Claude Code, and Gemini CLI directly inside IntelliJ IDEA.</p>
            <p>SkillOps generates platform-specific skill directories, structured <code>SKILL.md</code> content, supporting references, and optional scripts and assets. It also provides local Codex run insights with token usage and efficiency metrics.</p>
            <p>All generation, validation, and session analysis runs locally. SkillOps does not upload project files, prompts, session logs, credentials, or analytics.</p>
        """.trimIndent()
        changeNotes = """
            <ul>
                <li>Initial release of SkillOps.</li>
                <li>Creates project skills for Codex, Claude Code, and Gemini CLI.</li>
                <li>Generates <code>SKILL.md</code>, reference files, optional support folders, and Codex interface metadata.</li>
                <li>Adds deterministic validation for generated and existing skills.</li>
                <li>Adds local Codex run history with token usage and efficiency insights.</li>
            </ul>
        """.trimIndent()

        vendor {
            name = "spmisha134"
            url = pluginRepositoryUrl
        }

        ideaVersion {
            sinceBuild = "252"
            untilBuild = provider { null }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}
