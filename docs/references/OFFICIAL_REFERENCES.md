# Official References

## Codex skills

Official Codex skills documentation:

```text
https://developers.openai.com/codex/skills
```

Key points used by this project:

- A skill is a directory with required `SKILL.md`.
- Supporting files such as references, scripts, assets, and agent-specific metadata can be used.
- `SKILL.md` should be the entrypoint and can refer to supporting files.
- Skills can be selected implicitly or explicitly in Codex.

## Claude Code skills

Official Claude Code skills documentation:

```text
https://code.claude.com/docs/en/skills
```

Project skills are stored under `.claude/skills/<skill-name>/SKILL.md`.

## Gemini CLI skills

Official Gemini CLI skills documentation:

```text
https://geminicli.com/docs/cli/skills/
```

Project skills are stored under `.gemini/skills/<skill-name>/SKILL.md`.

## IntelliJ plugin publishing

Official JetBrains plugin publishing documentation:

```text
https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html
```

Key points used by this project:

- The first plugin publication must be uploaded manually.
- Later publishing can be automated after initial marketplace setup.

## IntelliJ plugin signing

Official JetBrains plugin signing documentation:

```text
https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
```

Key points used by this project:

- Signing helps ensure plugins are not modified during publishing and delivery.
- Signing secrets must not be committed.
