# EIZZOs-NPCs

**EIZZOs-NPCs** is a high-performance, packet-based NPC system for Paper 1.21.1. It empowers server owners to create deeply interactive characters with a sophisticated dialogue engine, advanced command chaining, and a complete GUI-based management system.

---

## 🌟 Core Pillars

### 🎭 Dynamic Skin & Appearance System
Give your NPCs life with a flexible skinning system.
*   **Instant Skinning:** Fetch any Minecraft player skin by username.
*   **Custom Models:** Support for various entity types (Villagers, Players, Zombies, Skeletons, and more).
*   **Cosmetic Toggles:** Manage nametag visibility, equipment (armor/items), and even player-specific capes.
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

## 🛠️ Advanced Features

*   **Hostile Mode:** NPCs can be toggled to chase and attack players, dealing damage based on the item they are holding.
*   **Intelligent Pathing:** NPCs can navigate obstacles, jump over 1-block hurdles, and respect collision rules.
*   **Everything via GUI:** Manage your entire NPC network through a clean, intuitive interface. No configuration files required.
*   **Property Library:** A specialized "bulk-edit" GUI allows you to select multiple property overrides and save them as a single action.
*   **Database Reliability:** Built-in support for SQLite and MariaDB ensures your data is persistent and safe.

---

## ⌨️ Commands & Permissions

### Commands
| Command | Description |
| :--- | :--- |
| `/npc` | Opens the main NPC List and Management GUI. |
| `/npc create <id> <name>` | Quickly spawn a default NPC at your location. |
| `/npc dialog <id> <node>` | Manually trigger a dialogue node for a player. |
| **Shift + Click NPC** | Direct shortcut to the Editor GUI for admins. |

### Permissions
| Permission | Description | Default |
| :--- | :--- | :--- |
| `eizzo.npcs.admin` | Full access to create, delete, and edit all NPCs and Dialogues. | OP |

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
