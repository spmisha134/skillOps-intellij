<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# skillops-intellij Changelog

## [Unreleased]

## [0.1.0]
### Added
- SkillOps plugin foundation.
- Project-view actions for creating and validating Codex skills.
- Deterministic skill generation and validation logic.
- Mandatory `agents/openai.yaml` generation with interface metadata.
- Run insights action for recent Codex sessions with skill detection, token usage, and efficiency metrics.
- Platform-specific skill creation under `.agents/skills`, `.claude/skills`, and `.gemini/skills`.
- Codex run history grouped by skill and timestamp, including invocation commands when available.
- Local-only session analysis with no remote telemetry or data upload.
