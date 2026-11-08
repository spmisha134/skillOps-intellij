# SkillOps 0.9.0

SkillOps 0.9.0 makes Run Insights easier to scan, expands Codex usage details, and prevents skill filesystem operations from blocking the IntelliJ UI.

### Fixed

- Run skill generation, filesystem refresh, and generated-skill validation outside the Swing event dispatch thread.
- Run skill discovery and standalone validation outside the Swing event dispatch thread.
- Run the recursive project VFS refresh after Copy Skills on its worker thread instead of the Swing event dispatch thread.
- Avoid holding an IntelliJ write action while performing blocking filesystem I/O.

### Added

- Show Run Insights metrics in a structured table with a plain-language explanation for every value.
- Show Codex primary and secondary rolling rate-limit windows, usage remaining, and local reset times when recorded.
- Show Codex plan, credit balance, credit availability, and spend-control status when recorded.
- Extract cache-write usage for both cumulative session totals and the latest context snapshot.
- Enable JetBrains Marketplace exception and UI-freeze reporting for actionable diagnostic evidence.

### Changed

- Separate cumulative session usage from the latest Codex context or Claude assistant call.
- Clarify model context capacity as an approximate context measure rather than account quota.
- Keep prompts and warnings in a compact notes area below the metrics table.
- Rename session model selectors to `Model to use` for clearer resume behavior.
- Allow users to cancel Run Insights while local sessions are loading, matching Resume Session loading.

## Compatibility

- Supports IntelliJ IDEA 2024.2 and newer releases.
