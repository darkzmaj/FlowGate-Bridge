# FlowGate Bridge

**Lets NPCs, menus and other plugins on your game server send players to another server through your proxy.**

Commands that only exist on the proxy fail with "Unknown command" when an NPC or a menu runs them on the game server. FlowGate Bridge registers those commands on the game server and asks the proxy to move the player. It runs on the game server and works with FlowGate Lite or any BungeeCord or Velocity proxy that handles `Connect` requests.

## Features

- **Server commands on the game server.** Commands such as `/survival` from a simple list in `config.yml`.
- **Works with NPCs and menus.** Citizens NPCs, menus, signs and other plugins that make a player run a command.
- **Fixed targets.** Each command always goes to the one server set in the config. Players cannot pick another target.
- **Spam limit.** One request per player every 500 ms.
- **Reload.** `/flowgate-bridge reload` applies config changes without a restart.
- **Small.** No database, no background tasks and no listeners for incoming messages.

## Supported platforms

- **Paper:** 1.21 and newer (tested on Paper 26.3 behind BungeeCord and Velocity).
- **Spigot, Purpur and Folia:** 1.21 and newer. Folia from 1.0.2. Plain Spigot was not tested.
- **Proxy:** BungeeCord, Waterfall, or Velocity with its BungeeCord plugin message channel on. FlowGate Lite is optional.
- **Java:** 21 or newer. Paper 26.3 needs Java 25.
- **Not supported:** servers older than 1.21.

## Installation

1. Download `FlowGate-Bridge-1.0.2.jar` from one of the links below.
2. Put it in the `plugins` folder of each game server and restart.
3. List your commands and target servers under `slash-servers` in `config.yml`. With FlowGate Lite, also allow those targets in its `plugin-messages.allowed-connect-servers`.

Full setup: [Install guide](https://kori.my/docs/flowgate-bridge/install/).

## Privacy

FlowGate Bridge opens no network connections of its own. It does not check for updates and sends no statistics. It only sends the target server name to your proxy through the player's own connection.

## Links

- **Documentation:** https://kori.my/docs/flowgate-bridge/
- **Download:** [GitHub](https://github.com/darkzmaj/FlowGate-Bridge/releases/latest), [Modrinth](https://modrinth.com/plugin/flowgate-bridge), [SpigotMC](https://www.spigotmc.org/resources/133640/)
- **Source code:** https://github.com/darkzmaj/FlowGate-Bridge
- **Support:** https://kori.my

Free and open source under the MIT licence. Made by DarkZmaj for KORI Network.
