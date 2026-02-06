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

| Command | Description |
| :--- | :--- |
| `/npc` | Opens the NPC list GUI. |
| `/npc create <id> <name>` | Creates a new NPC. |
| `/npc delete <id>` | Deletes an NPC. |
| `/npc set <id> <prop> <val>` | Sets various properties for an NPC (see below). |
| `/npc tp <id>` | Teleports an NPC to your location. |
| `/npc addcmd <id> <cmd>` | Adds an interaction command. |
| `/npc clearcmds <id>` | Clears all interaction commands. |
| `/npc help` | Shows the help menu. |

### Set Properties

The `/npc set <id> <property> <value>` command can modify the following properties:

| Property | Value Type | Description |
| :--- | :--- | :--- |
| `name` | String | The display name of the NPC (supports MiniMessage). |
| `type` | EntityType | The entity type (e.g., `VILLAGER`, `PLAYER`). |
| `skin` | String | The username to fetch the skin from. |
| `trackingmode` | `HEAD` / `BODY` / `NONE` | Sets how the NPC tracks players. |
| `trackingrange`| Number | The distance the NPC will track from. |
| `cape` | `true` / `false` | Toggles the cape visibility. |
| `collision` | `true` / `false` | Toggles player collision. |
| `npccollision`| `true` / `false` | Toggles collision with other NPCs. |
| `flying` | `true` / `false` | Makes the NPC appear to be flying. |
| `returntospawn`| `true` / `false` | If the NPC should return to its spawn point. |
| `nametag` | `true` / `false` | Toggles nametag visibility. |
| `sound` | Sound Key | The sound played on interaction. |
| `runmode` | `op` / `console` / `player` | The context for executing commands. |


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
