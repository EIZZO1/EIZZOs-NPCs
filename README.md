# EIZZOs-NPCs

**EIZZOs-NPCs** is a high-performance, packet-based NPC system for Paper 1.21.1. It empowers server owners to create deeply interactive characters with a sophisticated dialogue engine, advanced command chaining, and a complete GUI-based management system.

---

## 🌟 Core Pillars

### 🎭 Dynamic Skin & Appearance System
Give your NPCs life with a flexible skinning system.
*   **Instant Skinning:** Fetch any Minecraft player skin by username.
*   **Custom Models:** Support for various entity types (Villagers, Players, Zombies, Skeletons, and more).
*   **Cosmetic Toggles:** Manage nametag visibility, equipment (armor/items), and player-specific capes.
*   **Client-Side Rendering:** All NPCs are "fake" entities sent via packets, ensuring 0% impact on server tick rates or entity counts.

### 💬 Interactive Dialogue Engine
Create complex, branching stories using a node-based system.
*   **Branching Choices:** Present players with clickable options in chat (`[choice]`) that lead to different dialogue nodes.
*   **Temporary State Changes:** Change NPC properties (like making them hostile or changing their skin) specifically for the player interacting with them. These revert automatically when the conversation ends.
*   **Location Listening:** Pause a dialogue until the player reaches a specific block coordinate (`[listen]`).
*   **Dialogue Timeouts:** Automatic 60-second cleanup ensures NPCs aren't left in a modified state if a player walks away.

### 📜 Staged Command Queue
Chain multiple actions together to create cinematic interactions.
*   **Multi-Stage Actions:** Combine server commands, player commands, sound effects, and chat messages into a single sequence.
*   **Dynamic Tags:**
    *   `[wait]`: Add precise delays between actions.
    *   `[sound]`: Play any sound effect from the library or custom keys.
    *   `[msg]`: Send immersive chat messages from the NPC.
    *   `[set]`: Change NPC attributes (hostile, tracking, flying) on the fly.
    *   `[jump]`: Make the NPC perform a visual jump.
    *   `[home]`: Dynamically update the NPC's spawn/home location.

---

## ⌨️ Commands & Permissions

### Primary Commands
| Command | Description |
| :--- | :--- |
| `/npc` | Opens the main NPC List and Management GUI. |
| `/npc help` | Displays the help menu with all subcommands. |
| `/npc create <id> <name>` | Spawn a new NPC at your location. |
| `/npc list` | Opens the NPC list GUI. |
| `/npc delete <id>` | Permanently removes an NPC. |
| `/npc tp <id>` | Teleports the specified NPC to your location. |
| `/npc dialog <id> <node>` | Manually trigger a dialogue node for a player. |
| `/npc addcmd <id> <cmd>` | Add an interaction command (supports `[wait]` and `[sound]`). |
| `/npc clearcmds <id>` | Clears all commands from an NPC. |
| **Shift + Click NPC** | Direct shortcut to the Editor GUI for admins. |

### Property Commands (`/npc set <id> <property> <value>`)
| Property | Values | Description |
| :--- | :--- | :--- |
| `name` | `<text>` | Set the display name (supports MiniMessage). |
| `type` | `EntityType` | Set the NPC entity type. |
| `skin` | `<username>` | Fetch and apply a player skin. |
| `trackingmode`| `NONE`, `STILL`, `FOLLOW` | Set how the NPC tracks players. |
| `trackingrange`| `<number>` | Set the tracking distance. |
| `cape` | `true/false` | Toggle player cape visibility. |
| `collision` | `true/false` | Toggle player collision. |
| `npccollision`| `true/false` | Toggle collision with other NPCs. |
| `flying` | `true/false` | Toggle flying mode. |
| `returntospawn`| `true/false` | Should the NPC return to home when out of range. |
| `nametag` | `true/false` | Toggle nametag visibility. |
| `sound` | `<key>` | Set the interaction sound effect. |
| `runmode` | `player`, `op`, `console` | Set command execution context. |

### Permissions
| Permission | Description | Default |
| :--- | :--- | :--- |
| `eizzo.npcs.admin` | Full access to all NPC commands and features. | OP |

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.