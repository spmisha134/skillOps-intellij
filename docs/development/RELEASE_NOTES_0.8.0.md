# SkillOps 0.8.0

SkillOps 0.8.0 fixes Run Insights activity dates and expands the default session scan coverage.

### Fixed

- Use the latest recorded session event timestamp for Run Insights' `Updated` field instead of a potentially stale filesystem modification time.
- Sort displayed session history by actual session activity time.

### Changed

- Increase the default maximum sessions scanned from 200 to 250; the limit remains configurable in Settings.

## Compatibility

- Supports IntelliJ IDEA 2024.2 and newer releases.
