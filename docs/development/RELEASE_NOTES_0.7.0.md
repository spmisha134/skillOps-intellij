# SkillOps 0.7.0

SkillOps 0.7.0 improves session identification and Run Insights readability.

- Preserve and display Codex prompts in chronological order.
- Derive session titles from the latest meaningful prompt instead of injected `AGENTS.md` content.
- Put token metrics and efficiency information before prompt history in Run Insights.
- Replace vague large-session messages with transcript-size guidance explaining scan-time and context-overhead impact.
- Keep exact resume commands, working directories, and session IDs available.
- Use session event timestamps for Run Insights' `Updated` field, with file modification time only as a legacy fallback.
- Increase the default session scan limit from 200 to 250; the limit remains configurable in Settings.

## Compatibility

- Supports IntelliJ IDEA 2024.2 and newer releases.
