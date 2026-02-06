# EIZZOs-NPCs

Advanced client-side NPC system for the EIZZO Minecraft Network (Paper 1.21.1).

## Features

*   **Packet-Based NPCs:** Lightweight client-side NPCs using packet manipulation (via `ReflectionUtils`).
*   **Skin Support:** Custom skins support including layers.
*   **Database Persistence:** Supports MariaDB and SQLite for data storage.
*   **GUI Editor:** In-game GUI for managing NPCs (`NPCGUI`).
*   **Command Binding:** Bind server commands to NPC interactions.
*   **MiniMessage Support:** Uses Adventure/MiniMessage for rich text formatting.

## Commands

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/npc create <name> [type]` | Create a new NPC at your location. | `eizzo.npcs.admin` |
| `/npc list` | List all existing NPCs. | `eizzo.npcs.admin` |
| `/npc setname <id> <name>` | Change the display name of an NPC. | `eizzo.npcs.admin` |
| `/npc settype <id> <type>` | Change the entity type of an NPC. | `eizzo.npcs.admin` |
| `/npc setskin <id> <skin>` | Set the skin of an NPC. | `eizzo.npcs.admin` |
| `/npc addcmd <id> <cmd>` | Add a command to be executed on click. | `eizzo.npcs.admin` |
| `/npc removecmd <id> <index>` | Remove a bound command. | `eizzo.npcs.admin` |
| `/npc help` | Show help menu. | `eizzo.npcs.admin` |

## Permissions

*   `eizzo.npcs.admin`: Grants full access to all NPC commands and features. Default: OP.

## Technical Details

*   **Java Version:** 21
*   **Server Core:** Paper 1.21.1
*   **Build System:** Maven

## Building

To build the project:

```bash
mvn clean package
```

The output JAR will be in the `target/` directory.
