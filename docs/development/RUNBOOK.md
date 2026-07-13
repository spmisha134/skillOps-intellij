# Development and Publishing Runbook

This runbook describes how to build, verify, package, and publish SkillOps for IntelliJ IDEA.

## 1. Local development

### Run the plugin in a sandbox IDE

```bash
./gradlew runIde
```

### Run checks

```bash
./gradlew check
```

### Build the plugin

```bash
./gradlew buildPlugin
```

The plugin ZIP is generated under:

```text
build/distributions/
```

## 2. Before first release

Complete the repository cleanup checklist:

- [ ] Set the Gradle project group in `gradle.properties`
- [ ] Set the plugin id in `src/main/resources/META-INF/plugin.xml`
- [ ] Set the plugin name in `src/main/resources/META-INF/plugin.xml`
- [ ] Set the plugin description in `src/main/resources/META-INF/plugin.xml`
- [ ] Replace template README content with project-specific documentation
- [ ] Confirm source package name matches the project
- [ ] Confirm `./gradlew runIde` works
- [ ] Confirm `./gradlew check` passes
- [ ] Confirm `./gradlew buildPlugin` produces a plugin ZIP

## 3. Plugin identity

Recommended values:

```text
Plugin name: SkillOps
Repository: skillops-intellij
Default package: com.spmisha134.skillops
```

Recommended plugin description:

```text
Create and validate Codex/OpenAI skills directly inside IntelliJ IDEA.
```

## 4. Manual installation from local build

Build the plugin:

```bash
./gradlew buildPlugin
```

Install the ZIP manually:

```text
IntelliJ IDEA
→ Settings/Preferences
→ Plugins
→ ⚙️
→ Install Plugin from Disk...
→ Select ZIP from build/distributions/
```

## 5. Manual first publication

The first JetBrains Marketplace publication must be uploaded manually.

Steps:

1. Build the plugin ZIP.

```bash
./gradlew buildPlugin
```

2. Log in to JetBrains Marketplace.

3. Create or select the vendor profile.

4. Upload the plugin ZIP from:

```text
build/distributions/
```

5. Complete Marketplace listing:

```text
Plugin name
Description
Version
Change notes
Vendor
License
Tags
Compatibility
Screenshots if available
```

6. Submit for approval.

## 6. Marketplace ID

After approval, update README badges by replacing:

```text
MARKETPLACE_ID
```

with the real plugin ID from JetBrains Marketplace.

## 7. Plugin signing

Plugin signing should be configured before automated publishing.

Required secret values must not be committed to Git:

```text
CERTIFICATE_CHAIN
PRIVATE_KEY
PRIVATE_KEY_PASSWORD
```

## 8. Deployment token

Automated publishing requires a JetBrains Marketplace deployment token.

Store it as a GitHub secret.

Recommended secret name:

```text
PUBLISH_TOKEN
```

## 9. Automated publishing checklist

Before enabling automated publishing:

- [ ] First manual Marketplace publication completed
- [ ] Marketplace ID known
- [ ] README badges updated
- [ ] Plugin signing secrets configured
- [ ] Deployment token configured
- [ ] Release workflow verified
- [ ] Plugin version updated
- [ ] Change notes updated

## 10. Release checklist

Before creating a release:

- [ ] Product behavior matches `docs/product/PRODUCT_REQUIREMENTS.md`
- [ ] Architecture matches `docs/architecture/ARCHITECTURE.md`
- [ ] Specs are implemented or explicitly deferred
- [ ] `./gradlew check` passes
- [ ] `./gradlew buildPlugin` succeeds
- [ ] Plugin ZIP installs manually
- [ ] `New → SkillOps` action works
- [ ] `Validate SkillOps` action works
- [ ] Generated skill is created under `.agents/skills/<skill-name>/`
- [ ] Generated `SKILL.md` is small
- [ ] Generated reference files are created
- [ ] Generated `agents/openai.yaml` is created
- [ ] Generated skill includes instruction discovery for:
  - `AGENTS.md`
  - `agents.md`
  - `CLAUDE.md`
  - `claude.md`
  - `GEMINI.md`
  - `gemini.md`

## 11. Versioning

Use semantic versioning.

Recommended first public version:

```text
0.1.0
```

## 12. Do not publish until

- plugin name is final
- plugin id is final
- package name is final
- README is project-specific
- Marketplace description is project-specific
- generated plugin works in a sandbox IDE
