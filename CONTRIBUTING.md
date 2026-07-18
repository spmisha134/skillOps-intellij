# Contributing to SkillOps

Thanks for helping improve SkillOps.

## Development setup

- Install JDK 21.
- Use the checked-in Gradle wrapper; a separate Gradle installation is not required.
- Read `AGENTS.md`, `docs/product/PRODUCT_REQUIREMENTS.md`, and `docs/architecture/ARCHITECTURE.md` before changing product behavior or architecture.

## Making changes

- Keep changes focused and preserve the separation between IntelliJ UI code and testable domain logic.
- Do not add runtime network calls, telemetry, or uploads of project or Codex session data.
- Never commit credentials, local IDE state, session logs, absolute user paths, or files from unrelated projects.
- Add or update tests for generator, validator, parser, or formatter behavior.

## Verification

Run the full local release checks:

```bash
./gradlew check buildPlugin verifyPlugin
```

For UI changes, also launch the sandbox and verify the affected workflow manually:

```bash
./gradlew runIde
```

## Pull requests

Include:

- what changed and why
- affected files and user-facing behavior
- automated and manual verification performed
- screenshots for visible UI changes

By contributing, you agree that your contribution is licensed under the repository's Apache License 2.0.
