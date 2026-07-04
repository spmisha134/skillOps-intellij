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
        id = "com.spmisha134.buildskill"
        name = "BuildSkill"
        description = """
            <p>BuildSkill creates and validates repository-scoped Codex/OpenAI skills directly inside IntelliJ IDEA.</p>
            <p>It generates the official <code>.agents/skills/&lt;skill-name&gt;/</code> structure, keeps <code>SKILL.md</code> small, writes reference files, and validates the result before reporting success.</p>
        """.trimIndent()
        changeNotes = """
            <ul>
                <li>Initial release of BuildSkill.</li>
                <li>Adds project-view actions for creating and validating Codex skills.</li>
                <li>Generates <code>SKILL.md</code>, reference files, optional support folders, and mandatory <code>agents/openai.yaml</code>.</li>
                <li>Adds deterministic validation for generated and existing skills.</li>
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
}
