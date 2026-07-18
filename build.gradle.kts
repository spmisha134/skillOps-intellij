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
            <p><strong>Create reliable, repository-scoped skills for Codex, Claude Code, and Gemini CLI without leaving IntelliJ IDEA.</strong></p>
            <p>SkillOps removes the repetitive setup from skill authoring and helps you understand how Codex sessions use context and tokens.</p>
            <ul>
                <li>Create platform-specific skills with structured <code>SKILL.md</code> content and supporting references.</li>
                <li>Generate optional scripts and assets, plus Codex interface metadata.</li>
                <li>Validate Codex skills before committing them.</li>
                <li>Review local Codex run history, token usage, efficiency, rate-limit status, and sessions without skills.</li>
            </ul>
            <p><strong>Private by design:</strong> generation, validation, and session analysis run locally. SkillOps does not upload project files, prompts, session logs, credentials, or analytics.</p>
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
            name = "Sollymanul Islam"
            email = "spmisha134@gmail.com"
            url = pluginRepositoryUrl
        }

        ideaVersion {
            sinceBuild = "252"
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
