# EIZZOs-NPCs: The Ultimate NPC Management System

**EIZZOs-NPCs** is a state-of-the-art, high-performance NPC management system designed for the EIZZO Minecraft Network. Built for Paper 1.21.1, it provides an unparalleled level of customization through intuitive GUI menus, allowing server administrators to create deeply interactive and dynamic non-player characters without writing a single line of code.

## Key Features

- **Packet-Based & High-Performance:** NPCs are client-side entities manipulated via packets. They don't exist as real server entities, ensuring minimal impact on server performance.
- **Complete GUI-Based Configuration:** Say goodbye to complex commands and configuration files. Virtually every aspect of an NPC can be configured through a powerful and intuitive in-game GUI (`/npc`).
- **Advanced Staged Interactions:** Create complex sequences of actions for NPCs. An interaction can be a mix of commands, delays, sound effects, and chat messages, executed in any order you define.
- **Branching Dialogue System:** Design intricate, multi-choice conversations. Players can be presented with clickable options in chat, with each choice leading them down a different dialogue path or triggering unique events.
- **Deep Customization:**
    - **Appearance:** Control name (with MiniMessage support), entity type (player, villager, zombie, etc.), player skin, equipment (armor & items in hand), and visibility of capes and nametags.
    - **Behavior:** Define tracking modes (stand still, head-only tracking, or follow player), set tracking range, toggle flying, and toggle **Hostile Mode** (NPC chases and attacks nearby players).
- **Temporary Property Overrides:** Dialogues and interaction commands can temporarily change an NPC's properties for a specific player. For example, an NPC can become hostile only for the player currently interacting with it, reverting to its original state once the interaction ends.
- **Property Preset Library:** A powerful GUI library to add property changes in dialogues. It features a multi-selection list where you can toggle multiple attributes (showing "Normal" vs "New" states) and save them all as a single `[set]` action using the **DONE** button.
- **Dynamic Dialogue Flow:** Dialogues can now wait for specific player actions, such as reaching a certain coordinate, or trigger state changes like updating the NPC's home location on the fly.
- **Improved Pathing & Physics:** NPCs now feature intelligent movement, including the ability to jump over obstacles and a robust collision system to prevent phasing through walls or low ceilings.
- **Flexible Command Execution:** Commands triggered by NPCs can be run from three different contexts: by the **Player**, by the **Console**, or by the **Player with temporary OP**.
- **Robust Data Persistence:** All NPC data is reliably saved to a database, with support for both **SQLite** (for easy setup) and **MariaDB/MySQL** (for larger networks).

## The GUI: Your Central Control Panel

The entire plugin is managed through the main command: `/npc`. This opens a clean, easy-to-navigate GUI.

1.  **NPC List (`/npc`):** View all your NPCs, create new ones, or select one to edit.
2.  **Main Editor:** Once you select an NPC, you get access to five sub-menus:
    - **Appearance:** Change ID, name, skin, entity type, and toggle cosmetics like capes and nametags.
    - **Behavior:** Configure how the NPC looks at and follows players, its movement abilities, toggle **Hostile Mode** (NPC chases and attacks nearby players), and teleport it instantly.
    - **Interactions:** This is the heart of the plugin. Access the powerful **Command Editor** and the **Dialogue Manager**.
    - **Physics:** Toggle collision with players and other NPCs.
    - **Equipment:** A visual editor to place armor and items directly onto your NPC.

### The Staged Command Editor

Inside the **Interactions** menu, you can define a sequence of events that happen when a player clicks the NPC. You aren't limited to one command; you can add multiple "stages":
- **Commands:** Add any server or player command.
- **Delays:** Pause the sequence for a specific duration (e.g., `[wait] 1.5` for 1.5 seconds).
- **Sounds:** Play a sound effect from a built-in library or by specifying any custom sound key.
- **Property Sets:** Temporarily modify NPC attributes for the player (e.g., `[set] hostile=true;trackingmode=FOLLOW`).
- **Location Listen:** Pause the sequence until the player reaches a specific block coordinate (e.g., `[listen] 100 64 -200`).
- **Jump:** Trigger a single visual jump for the NPC (`[jump]`).
- **Update Home:** Set the NPC's home/spawn location to specific coordinates or the player's position during configuration (`[home] world x y z yaw pitch`).
- **Dialogue Timeouts:** Interactions that wait for player input (like `[choice]` or `[listen]`) have a default 60-second timeout. If the player doesn't react in time, the dialogue ends and the NPC is restored.

### The Branching Dialogue Manager

Also in the **Interactions** menu, the Dialogue Manager allows you to create "nodes" (like scenes in a conversation). For each node, you can add a sequence of events, just like the command editor.

Crucially, you can add a `[choice]` stage, which presents clickable options to the player in chat.
- **Format:** `[choice] Say Hello=node_greeting | Ask Quest=node_quest_start`
- Each option links to another dialogue node, allowing you to build complex, branching stories and quests.

## Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/npc` | Opens the main NPC management GUI. | `eizzo.npcs.admin` |
| `/npc dialog <id> <node>`| Manually trigger a dialogue node for a player. | `eizzo.npcs.admin` |

The permission `eizzo.npcs.admin` (default: OP) grants full access to the GUI and all management features. Additionally, shift-clicking an NPC while having this permission will open its editor directly.

## Technical Details

- **Java Version:** 21
- **Server Core:** Paper 1.21.1
- **Build System:** Maven

## Building

To build the project:
```bash
mvn clean package
```
The output JAR will be in the `target/` directory.

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.