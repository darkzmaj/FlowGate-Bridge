# FlowGate-Bridge

A lightweight backend plugin that lets players and Citizens NPCs executing as players use configured slash commands to request a proxy server switch. This is independent of FlowGate Lite: Bridge runs on the backend; Lite runs on BungeeCord.

## Install

Copy `FlowGate-Bridge-1.0.1.jar` to your Paper backend's `plugins/` directory and configure:

```yaml
slash-servers:
  lobby: Lobby-EU
  survival: survival
```

Command names are case-insensitive; target server names preserve their case and must exactly match the proxy configuration. Names must contain letters/numbers, underscores or hyphens; server targets may also contain dots, up to 128 characters. `/flowgate-bridge reload` requires `flowgatebridge.admin` (default: operator). Invalid YAML or routes retain the previous configuration. Removed commands and their namespaced aliases are unregistered on reload; other plugins' commands are preserved.

`flowgatebridge.use` allows route commands and defaults to true for backward compatibility. Explicitly deny it to restrict users. A 500 ms per-player cooldown prevents repeated message spam; entries are removed on quit/shutdown. Connection messages do not acknowledge success: the proxy controls routing and reports connection failures.

## Compatibility and security

The plugin retains Java 21 bytecode and `api-version: 1.21`. Current target: **Paper 26.3 build 194**, requiring **Java 25**. Older Paper/Spigot servers retain command-map compatibility fallbacks but require separate runtime testing. Folia is not declared supported. Waterfall is discontinued; do not claim latest Minecraft support using it.

The wire format is unchanged: `writeUTF("Connect")`, `writeUTF(target)`, through `BungeeCord` (`bungeecord:main` on the network). No input becomes a console command. Bridge registers only an outgoing channel, accepts no inbound requests, and can request a switch only for the executing player. Arbitrary command arguments do not control the destination.

Use maintained BungeeCord (build 2104 for protocol 777 / Minecraft 26.3). With FlowGate Lite, explicitly permit backend source names and target names in its `plugin-messages` policy. Velocity can provide the Bungee message channel when `advanced.bungee-plugin-message-channel` is enabled. FlowGate Lite itself cannot load on Velocity. Protect offline-mode backends from direct public access, enable appropriate proxy forwarding, and apply destination access controls on the proxy. A compromised trusted backend remains inside the proxy trust boundary.

Citizens example: `/npc command add -p survival`. This requires the NPC action to execute as a real connected player.

## Build

Use JDK 25 and `bash gradlew clean build`. Output: `build/libs/FlowGate-Bridge-1.0.1.jar`. No runtime dependencies are shaded. The MIT [LICENSE](LICENSE) is included in the JAR. See [CHANGELOG.md](CHANGELOG.md) and [AUDIT.md](AUDIT.md) for validation and limitations.

## Release downloads and publishing

Install the compiled **FlowGate-Bridge-<version>.jar** from [GitHub Releases](https://github.com/darkzmaj/FlowGate-Bridge/releases); the generated source archives are not installable plugins. Release assets include a SHA-256 file.

The **Publish release JAR** workflow runs for new `vMAJOR.MINOR.PATCH` tags. To attach assets to an existing release, run it manually from `main` with the exact version tag (for example `v1.0.1`). It checks out the resolved tag commit, uses Java 25, runs `clean build` and existing tests, validates plugin metadata and bytecode, and preserves the JAR as an Actions artifact before publishing. The build has read-only repository access; only the separate publishing job has `contents: write`. Existing assets are compared and never overwritten. The audited release tag and original JAR checksum are pinned for v1.0.1; a mismatch stops publication.

Repository Actions must be enabled and permit the workflow's `GITHUB_TOKEN` to request `contents: write` (organization policy must permit it). No personal access token is required. No tag or release needs to be deleted or recreated. See [release notes](docs/releases/v1.0.1.md) for verified platforms and limitations.
