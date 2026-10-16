# 1.0.1 — 2026-10-16

- Preserve proxy target case; normalize only command names with Locale.ROOT.
- Validate command/target types, control characters, lengths, duplicate command names and unsupported route values.
- Reject failed reloads without losing working routes; unregister removed/owned commands and aliases on reload/disable while preserving unrelated commands.
- Prefer the public Paper command map, with legacy reflection fallbacks and actionable startup failures. Remove mappings by key because current Paper does not support iterator removal.
- Add the default-allow flowgatebridge.use permission, a 500 ms per-player request cooldown, quit/shutdown cleanup and admin tab completion.
- Retain the fixed Connect wire format and outgoing-only trust boundary; replace unnecessary Guava serialization with bounded JDK serialization.
- Upgrade Paper API 1.21.11 snapshot to stable 26.3 build 194; remove the obsolete development server task targeting 1.21. Pin Gradle 9.1 with checksum, reproducible unshaded artifacts, regression tests and CI. Include the existing MIT license in artifacts.
