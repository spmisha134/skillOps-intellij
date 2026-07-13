# SkillOps - Claude Code Guide

## Prerequisites

- **Java 21** — install via your preferred toolchain manager.
- **Gradle** — use the `./gradlew` wrapper; no separate Gradle installation is required.

For local development, JDK 21 and the wrapper are sufficient.

## Architecture Overview

SkillOps is an IntelliJ IDEA plugin that creates and validates repository-scoped Codex/OpenAI skills under:

```text
.agents/skills/<skill-name>/
```

The plugin is local-first and deterministic. It does not call OpenAI APIs, remote documentation, or marketplace services at runtime.

**IntelliJ integration**

- `actions/CreateSkillAction.kt` registers the `New → SkillOps` entry point.
- `actions/ValidateSkillAction.kt` validates an existing skill folder.
- `ui/` owns IntelliJ/Swing dialog code.
- `presentation/` owns IDE messages and validation result presentation.

**Domain logic**

- `model/` contains plain Kotlin request/result/validation data classes.
- `generator/` normalizes names, renders templates, resolves paths, and writes skill files.
- `validator/` runs deterministic validation rules against generated or existing skills.

**Where to look by change type**

| Change area | Start here |
|---|---|
| Create action behavior | `src/main/kotlin/com/spmisha134/skillops/actions/CreateSkillAction.kt` |
| Validate action behavior | `src/main/kotlin/com/spmisha134/skillops/actions/ValidateSkillAction.kt` |
| Dialog fields and validation | `src/main/kotlin/com/spmisha134/skillops/ui/` |
| Skill file generation | `src/main/kotlin/com/spmisha134/skillops/generator/SkillGenerator.kt` |
| Generated templates | `src/main/resources/templates/generated-skill/` |
| Validation rules | `src/main/kotlin/com/spmisha134/skillops/validator/rules/` |
| Product behavior | `docs/product/PRODUCT_REQUIREMENTS.md` |

## Module Structure

This is a single-module IntelliJ plugin.

| Path | Purpose |
|---|---|
| `src/main/kotlin/` | Main plugin and domain code |
| `src/main/resources/META-INF/plugin.xml` | IntelliJ plugin descriptor and action registration |
| `src/main/resources/templates/` | Generated skill templates |
| `src/test/kotlin/` | Unit tests for generation and validation |
| `docs/product/` | Product requirements |
| `docs/architecture/` | Architecture notes |
| `docs/development/` | Build, release, and publishing runbook |

## Build and Validation

### Available commands

**Compile only** — fastest, catches syntax/type errors:

```bash
./gradlew compileKotlin compileTestKotlin
```

**Unit tests and plugin checks**:

```bash
./gradlew check
```

**Build plugin only** — produces the ZIP without running tests:

```bash
./gradlew buildPlugin
```

**Full local release check**:

```bash
./gradlew check buildPlugin
```

**Plugin compatibility verification**:

```bash
./gradlew verifyPlugin
```

### Running the plugin locally

To launch an IntelliJ instance with the plugin installed for manual validation:

```bash
./gradlew runIde
```

The sandbox is stored under `build/idea-sandbox` by the IntelliJ Platform Gradle Plugin defaults. Running `./gradlew clean` may remove local build and sandbox state.

## Development Rules

- Read `README.md` before making changes.
- Read `docs/product/PRODUCT_REQUIREMENTS.md` before implementing product behavior.
- Read `docs/architecture/ARCHITECTURE.md` before changing package structure or core design.
- Keep changes small and focused.
- Keep IntelliJ-specific code out of pure generator and validator logic.
- Do not introduce AI calls, remote documentation fetching, marketplace publishing automation, or unrelated features unless the relevant spec requires it.
- After changes, explain:
  - what changed
  - how to test it
  - which files were affected

## Dependency Management

Dependencies are declared directly in `build.gradle.kts`.

Before adding a dependency:

- prefer existing IntelliJ Platform APIs or Kotlin/JDK APIs
- keep domain logic testable without launching IntelliJ
- avoid runtime network dependencies

After changing build dependencies, run:

```bash
./gradlew check buildPlugin
```
