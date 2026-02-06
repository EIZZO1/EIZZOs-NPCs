# EIZZOs-NPCs

**EIZZOs-NPCs** is a high-performance, packet-based NPC system for Paper 1.21.1. It empowers server owners to create deeply interactive characters with a sophisticated dialogue engine, advanced command chaining, a robust combat system, and a complete GUI-based management system.

---

## 🌟 Core Pillars

### 🎭 Dynamic Appearance & Skin System
Give your NPCs life with a flexible skinning system.
*   **Instant Skinning:** Fetch any Minecraft player skin by username.
*   **Custom Models:** Support for various entity types (Villagers, Players, Zombies, Skeletons, and more).
*   **Cosmetic Toggles:** Manage nametag visibility, equipment (armor/items), and player-specific capes.
*   **Client-Side Rendering:** All NPCs are "fake" entities sent via packets, ensuring 0% impact on server tick rates.

### 💬 Advanced Dialogue Engine
Create complex, branching stories using a node-based system.
*   **Secure Execution:** Uses a one-time session token system to prevent players from manually bypassing dialogue steps via commands.
*   **Sequential Storytelling:** "Dialogue Once" mode allows NPCs to cycle through nodes (`start` -> `start1` -> `start2`...) as players progress through a story.
*   **Branching Choices:** Clickable chat options (`[choice]`) that lead to different nodes.
*   **Distance Validation:** Automatic checks ensure players stay near the NPC during interaction.
*   **Styled Dialogue:** Conversations use a premium bold Aqua/Cyan format: `[NPCName]: message`.

### ⚔️ Combat & Health System (Player-Specific)
Turn NPCs into interactable combatants or targets.
*   **Natural Physics:** Realistic gravity (1.0) and velocity-based animations for a grounded, heavy feel.
*   **God Mode Toggle:** Enable/Disable whether an NPC can take damage.
*   **Per-Player Health:** NPCs track health uniquely for every player. One player killing an NPC does not affect its visibility for others.
*   **Visual Health Bars:** Packet-based `TextDisplay` health bars that update in real-time.
*   **Custom Respawns:** Set a specific delay for NPCs to "reappear" after being defeated.

### 💰 Integrated Reward System
Reward players for their interactions or victories.
*   **Kill Rewards:** Give money or tokens when an NPC is defeated.
*   **Dialogue Rewards:** Use the `[reward]` tag to grant rewards at any point during a conversation.
*   **Configurable Messages:** Fully customize reward notifications in `config.yml`.
*   **Dual Economy:** Full support for **Vault** and **EIZZOs-Tokens**.

---

## 📜 Action Tags
Used in commands and dialogue sequences:
*   `[wait] <seconds>`: Precise delays between actions.
*   `[sound] <key>`: Play sound effects (supports library and custom keys).
*   `[msg] <text>`: Private chat messages from the NPC.
*   `[choice] Label=Node | Label2=Node`: Interactive branching.
*   `[set] key=val`: Change NPC attributes on the fly (temp or permanent).
*   `[reward]`: Trigger the NPC's configured Vault/Token rewards.
*   `[jump]`: Smooth, velocity-based jump animation.
*   `[listen] <coords>`: Pause until the player reaches a location.
*   `[home] <coords>`: Per-player (client-side) temporary home update.

---

## ⌨️ Commands & Permissions

### Primary Commands
| Command | Description |
| :--- | :--- |
| `/npc` | Opens the main NPC List and Management GUI. |
| `/npc create <id> <name>` | Spawn a new NPC at your location. |
| `/npc tp <id> [world x y z yaw pitch]` | Teleports the specified NPC to you or exact coords. |
| `/npc debug` | Toggles an 8-block grid visualization of the NPC tracking cube. |
| `/npc help` | Displays a premium, permission-aware help menu. |
| **Shift + Click NPC** | Direct shortcut to the Editor GUI for admins. |

### Property Commands (`/npc set <id> <property> <value>`)
| Property | Description |
| :--- | :--- |
| `name`, `type`, `skin` | Change the NPC's fundamental appearance. |
| `godmode` | `true/false` - Toggle if the NPC can be damaged. |
| `location` | Set exact coords: `<world> <x> <y> <z> <yaw> <pitch>`. |
| `respawndelay` | Set time in seconds before an NPC respawns. |
| `maxhealth` | Set the NPC's maximum HP. |
| `trackingmode` | `NONE`, `STILL`, `FOLLOW`. |
| `runmode` | `player`, `op`, `console`. |

---

## 📄 License
This project is licensed under the **MIT License**.