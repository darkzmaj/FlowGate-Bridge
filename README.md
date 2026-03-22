# FlowGate-Bridge

A lightweight Paper/Spigot plugin that allows Citizens NPCs (or players) to execute `/servername` commands and seamlessly connect to servers through a BungeeCord/Waterfall/FlameCord proxy.

## ✨ Features

* Dynamic commands from config (`/servername`)
* Works with Citizens NPCs (player execution)
* No permissions required
* No command conflicts
* Extremely lightweight (no listeners, no loops)
* Fully proxy-compatible (BungeeCord, Waterfall, FlameCord)
* Zero hardcoded values — fully configurable

---

## 📦 How It Works

FlowGate-Bridge acts as a backend bridge:

```text
Player/NPC → /command → Bridge (Paper) → Plugin Message → Proxy → Server Switch
```

* The plugin registers commands on the backend server
* When executed, it sends a plugin message to the proxy
* Your proxy plugin (e.g. FlowGate-Lite) handles the actual server connection

---

## ⚙️ Installation

1. Install a proxy:

   * BungeeCord / Waterfall / FlameCord

2. Install your proxy-side plugin (e.g. FlowGate-Lite)

3. Install **FlowGate-Bridge** on your backend server (Paper/Spigot)

4. Configure both plugins with the same `slash-servers` section

5. Restart server

---

## 📝 Configuration

`config.yml`

```yaml
slash-servers:
  survival: survival
  skyblock: skyblock
  wonderland: wonderland
```

### Format

```
<command>: <target-server>
```

* `command` → what players/NPCs execute (`/survival`)
* `target-server` → server name in your proxy config

---

## 🔁 Reload Command

```bash
/flowgate-bridge reload
```

* Reloads config
* Re-registers commands
* Applies changes instantly

---

## 🤖 Citizens NPC Example

```bash
/npc command add -p survival
```

* Player clicks NPC → executes `/survival`
* Bridge forwards to proxy
* Player is connected

---

## ⚠️ Important Notes

* Commands must match your proxy configuration
* Target servers must exist on proxy
* This plugin does **not** handle server switching itself
* Requires proxy plugin to complete routing

---

## 🧠 Why This Plugin Exists

Minecraft processes commands on backend servers first.
Without this bridge, proxy-only commands like `/survival` will fail with:

```
Unknown command
```

FlowGate-Bridge solves this cleanly by:

* registering commands on backend
* forwarding them properly to proxy

---

## 🚀 Performance

* No event listeners
* No async tasks
* No heavy logic

Overhead is effectively zero.

---

## 🔗 Compatibility

* Paper / Spigot (1.16+ recommended)
* BungeeCord / Waterfall / FlameCord
* Citizens (optional)

---

## 📄 License

MIT License (or your preferred license)

---

![Replace this with a description](https://cdn.modrinth.com/data/cached_images/c0ed4260b1584b2f8a3d8cd12068aebbdbf85179.png)
