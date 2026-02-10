# EIZZOs-NPCs

**Packet-Based NPC System** with intelligent movement, branching dialogue, combat mechanics, and per-player state tracking.

---

## 🎯 Features

### 🤖 Advanced NPC System
- **Packet-Based Rendering** - Client-side NPCs using network packets (not server entities)
- **Per-Player State** - Each player has unique NPC health, position, and dialogue progress
- **35+ Configurable Properties** - Customize everything from tracking range to rewards
- **Visual Editor GUI** - In-game NPC management with chat-based property editing

### 🎭 Dialogue System
- **Branching Conversations** - Multi-node dialogue trees with clickable choices
- **Action Sequences** - Messages, sounds, waits, teleports, rewards in dialogue
- **One-Time Tokens** - Secure choice validation prevents command exploitation
- **Dialogue Once Mode** - Track viewed nodes per player, skip seen content

### ⚔️ Combat & Tracking
- **Three Tracking Modes** - NONE (static), STILL (rotate only), FOLLOW (pathfind)
- **Hostile NPCs** - Attack players within range with weapon scaling
- **Intelligent Pathfinding** - Navigate obstacles, jump over blocks, avoid collisions
- **Per-Player Health** - Killing an NPC for one player doesn't affect others

### 🎨 Customization
- **Full Skin Support** - Mojang API (usernames) or PNG file uploads via Mineskin
- **Equipment System** - Armor and hand items with full NBT support
- **Sound Effects** - Custom interaction sounds per NPC
- **Debug Visualization** - Particle grid showing tracking ranges

### 💰 Reward Integration
- **Vault Economy** - Give money on NPC kills
- **EIZZOs-Tokens** - Award custom tokens (soft dependency)
- **Reward Limits** - Optional per-player claim caps
- **Custom Messages** - Configurable reward notifications

---

## 📝 Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/npc` | `eizzo.npcs.admin` | Open NPC list GUI |
| `/npc help` | - | Show help menu |
| `/npc debug` | `eizzo.npcs.admin` | Toggle debug particles (8-block cube visualization) |
| `/npc create <id> <name>` | `eizzo.npcs.admin` | Create new NPC at your location |
| `/npc list` | `eizzo.npcs.admin` | Open NPC list GUI |
| `/npc delete <id>` | `eizzo.npcs.admin` | Permanently delete NPC |
| `/npc set <id> <property> <value>` | `eizzo.npcs.admin` | Set NPC properties (see below) |
| `/npc tp <id> [world x y z yaw pitch]` | `eizzo.npcs.admin` | Teleport NPC to your location or coords |
| `/npc addcmd <id> <command>` | `eizzo.npcs.admin` | Add interaction command/action |
| `/npc clearcmds <id>` | `eizzo.npcs.admin` | Clear all commands from NPC |
| `/npc dialog <id> <node> [token] [player]` | Admin/Token | Execute dialogue node (console use requires player arg) |

### NPC Set Properties

| Property | Type | Description |
|----------|------|-------------|
| `name` | String | Display name (supports MiniMessage) |
| `type` | EntityType | Mob type (VILLAGER, ZOMBIE, SKELETON, PLAYER, etc.) |
| `skin` | String | Player username or PNG filename in skins/ folder |
| `trackingmode` | NONE/STILL/FOLLOW | Tracking behavior |
| `trackingrange` | Double | Detection range in blocks |
| `hostile` | true/false | Attacks players in range |
| `cape` | true/false | Show cape for PLAYER type NPCs |
| `collision` | true/false | Players can collide with NPC |
| `npccollision` | true/false | NPCs collide with other NPCs |
| `flying` | true/false | No gravity, 3D movement |
| `returntospawn` | true/false | Returns to spawn when player leaves range |
| `nametag` | true/false | Nametag visibility |
| `sound` | Sound key or "none" | Sound on interaction (e.g., `ui.button.click`) |
| `godmode` | true/false | Takes no damage |
| `respawndelay` | Integer | Ticks before respawn after death (20 = 1 second) |
| `runmode` | op/console/player | Command execution mode |
| `location` | world x y z yaw pitch | Manual location setting |

---

## 🔐 Permissions

| Permission | Description |
|-----------|-------------|
| `eizzo.npcs.admin` | Full access to all NPC commands and editor GUIs |

**Note:** All commands except `/npc help` require admin permission. Dialogue execution requires either a valid session token OR admin permission.

---

## 🛠️ Configuration

### Main Config (`config.yml`)
```yaml
config-version: 1.1

tracking:
  max-range: 50.0              # Server-side culling range (cube)

database:
  type: "sqlite"               # "sqlite" or "mysql"
  host: "localhost"
  port: 3306
  database: "minecraft"
  username: "root"
  password: ""
  pool-size: 10                # MySQL only

logging:
  dialogue-to-console: true    # Log dialogue executions

rewards:
  enabled: true
  max-amount: 1000.0           # Maximum reward per kill
  use-vault: true              # Enable Vault economy
  use-eizzos-tokens: false     # Enable EIZZOs-Tokens
  vault-message: "<green>+ ${amount} <gray>(NPC Kill)"
  token-message: "<gold>+ {amount} {currency} <gray>(NPC Kill)"
```

### Database
- **Default:** SQLite at `plugins/EIZZOs-NPCs/npcs.db`
- **Optional:** MySQL support via HikariCP connection pooling
- **Tables:** `npcs`, `npc_commands`, `npc_dialogues`, `npc_equipment`, `player_seen_nodes`, `player_reward_counts`

---

## 🎬 Interaction Mechanics

### Player Interactions
- **Left-Click (Attack):** Damages NPC if not in GodMode; applies knockback and hurt animation
- **Right-Click (Interact):** Triggers dialogue or commands
- **Sneak + Right-Click (Admin):** Opens NPC editor GUI

### Combat System
**Weapon Damage Scaling:**
- Base: Swords (6), Axes (7), Pickaxes (4), Shovels (3), Hoes (2), Fist (1)
- Material Bonuses: Netherite +2, Diamond +1.5, Iron +1.0, Stone +0.5
- **Critical Hits:** 1.5x damage when falling (not on ground)

**Hostile Mode:**
- NPCs chase and attack players within tracking range
- 1-second attack cooldown per player-NPC pair
- Knockback applied to attacked players
- Ignores Creative/Spectator mode players

---

## 💬 Dialogue System

### Dialogue Actions

Actions are executed in order, one per line in dialogue nodes:

| Action | Format | Description |
|--------|--------|-------------|
| `[msg]` | `[msg] Hello %player%!` | Send formatted NPC message |
| `[wait]` | `[wait] 2.5` | Pause dialogue for N seconds |
| `[sound]` | `[sound] entity.villager.ambient` | Play sound effect |
| `[choice]` | `[choice] Option1=next_node \| Option2=another` | Show clickable buttons (generates tokens) |
| `[listen]` | `[listen] [world] x y z` | Wait for player to move to specific block |
| `[reward]` | `[reward]` | Give Vault/Token rewards to player |
| `[home]` | `[home] world x y z yaw pitch` | Teleport NPC to location |
| `[jump]` | `[jump]` | Apply upward velocity (1.6 blocks/tick) |
| `[set]` | `[set] property1=value; property2=value` | Override NPC properties for this player |

### Dialogue Once Mode
When enabled, tracks which nodes each player has viewed:
- Automatically skips to next unseen `start` node (start, start1, start2, etc.)
- Falls back to standard commands if all nodes seen
- Stored in `player_seen_nodes` database table

### Token Security
- Each `[choice]` generates random 6-character hex tokens
- Tokens are one-time use and expire on selection
- Prevents manual `/npc dialog` command exploitation
- Admins bypass token validation (but still require token field)

---

## 🚶 Movement & Physics

### Tracking Modes

| Mode | Behavior |
|------|----------|
| `NONE` | NPC doesn't track; stays at spawn |
| `STILL` | Rotates head to face player but doesn't move |
| `FOLLOW` | Pathfinds toward player with collision avoidance |

### Physics Constants
- **Gravity:** 1.0 blocks/tick (realistic falling)
- **Movement Speed:** 0.4 blocks/tick (normal), 0.6 blocks/tick (hostile)
- **Stop Distance:** 2.5 blocks (normal), 1.5 blocks (hostile)
- **Jump Height:** 1.1 blocks (auto-jumps obstacles ≤1 block tall)
- **Step-Up Height:** 0.6 blocks (step up ledges without jumping)

### Collision System
- **Player Collision:** NPCs push players away if `collidable=true`
- **NPC-to-NPC:** Collision only if both NPCs visible and `npcCollision=true`
- **Obstacle Avoidance:** Navigates around solid blocks
- **Spectator Immunity:** Spectator mode players don't trigger tracking/collision

---

## 🎨 Skin System

### Player Skins
```bash
/npc set <id> skin Notch
```
- Fetches from Mojang API: Username → UUID → Texture properties
- Fallback: MC-Heads API if Mojang fails
- Async background processing (non-blocking)

### Custom PNG Skins
1. Place PNG file in `plugins/EIZZOs-NPCs/skins/`
2. Run `/npc set <id> skin filename.png`
3. Plugin uploads to Mineskin API for texture conversion
4. Texture applied to NPC automatically

---

## 📦 Equipment & Armor

Equipment is stored per-slot in the database:
- **Slots:** HAND, OFF_HAND, HEAD, CHEST, LEGS, FEET
- **Full NBT Support:** Custom items, enchantments, names preserved
- **Set via Editor GUI:** Drag items into slots

---

## 💰 Reward System

### Vault Economy
```yaml
rewards:
  use-vault: true
  vault-message: "<green>+ ${amount} <gray>(NPC Kill)"
```
- Requires Vault plugin installed
- Direct economy deposit on NPC kill
- Configurable message with `{amount}` placeholder

### EIZZOs-Tokens Integration
```yaml
rewards:
  use-eizzos-tokens: true
  token-message: "<gold>+ {amount} {currency} <gray>(NPC Kill)"
```
- Soft dependency on EIZZOs-Tokens plugin
- Awards custom token types on kill
- Per-NPC token type configuration (`rewardTokenId`)

### Reward Limits
```yaml
# In NPC properties
rewardLimit: 10  # Max 10 claims per player (0 = unlimited)
```
- Tracks claims per player in `player_reward_counts` table
- Prevents reward farming

---

## 📚 Examples

### Simple Merchant NPC
```bash
/npc create merchant "Merchant Bob"
/npc set merchant type VILLAGER
/npc set merchant trackingmode STILL
/npc set merchant trackingrange 5
/npc set merchant sound entity.villager.trade
/npc addcmd merchant [opengui] merchant_shop
```

### Hostile Guard NPC
```bash
/npc create guard "Castle Guard"
/npc set guard type ZOMBIE
/npc set guard hostile true
/npc set guard godmode false
/npc set guard trackingmode FOLLOW
/npc set guard trackingrange 15
/npc set guard vaultreward 50.0
```

### Quest NPC with Dialogue
Create an NPC with branching dialogue:

1. **Create NPC:**
   ```bash
   /npc create questgiver "Quest Master"
   /npc set questgiver type PLAYER
   /npc set questgiver skin Notch
   /npc set questgiver trackingmode STILL
   ```

2. **Edit via GUI:**
   - Sneak + Right-Click the NPC
   - Navigate to "Dialogue Editor"
   - Create `start` node:
     ```
     [msg] <yellow>Greetings, traveler!
     [wait] 1.5
     [msg] <gray>I have a quest for you.
     [choice] Accept Quest=quest_accept | Decline=quest_decline
     ```

3. **Add Follow-Up Nodes:**
   - `quest_accept` node:
     ```
     [msg] <green>Excellent! Bring me 10 diamonds.
     [sound] entity.player.levelup
     [player] quest accept diamond_quest
     [reward]
     ```
   - `quest_decline` node:
     ```
     [msg] <red>Perhaps another time...
     [sound] entity.villager.no
     ```

### Moving NPC with Physics
Create an NPC that jumps to a platform:

```bash
/npc create jumper "Parkour Pro"
/npc set jumper type PLAYER
/npc addcmd jumper [msg] Watch this!
/npc addcmd jumper [wait] 1
/npc addcmd jumper [jump]
/npc addcmd jumper [wait] 1
/npc addcmd jumper [home] world 100 75 200 0 0
```

**IMPORTANT:** When using `[home]` or `[jump]` in dialogue, the plugin automatically clears cached location to prevent snap-back bugs.

---

## 🔧 Troubleshooting

### NPC not spawning?
- Check console for errors on `/npc create`
- Verify database is accessible (SQLite file or MySQL connection)
- Ensure chunk is loaded

### NPC snapping back to spawn?
- **CRITICAL:** When using teleport actions in dialogue (`[home]`), ensure plugin clears cached location
- This is handled automatically by the dialogue system
- Manual teleports via `/npc tp` work correctly

### Dialogue choices not working?
- Verify token generation in debug logs (`dialogue-to-console: true`)
- Check player has clicked the text component (not just typed command)
- Ensure dialogue action format: `[choice] Option1=node1 | Option2=node2` (pipe-separated)

### Skin not loading?
- **Mojang API:** Verify username is valid and skin is available
- **PNG Upload:** Check file exists in `plugins/EIZZOs-NPCs/skins/`
- **Mineskin API:** Check console for API errors (rate limiting)
- **Fallback:** MC-Heads API used if Mojang fails

### Combat not working?
- Verify `godmode` is set to `false`
- Check `hostile` is `true` for attacking NPCs
- Ensure player is in Survival/Adventure mode (not Creative/Spectator)
- Left-click detection uses packet interception (works on Paper 1.21.1)

### Rewards not giving?
- **Vault:** Verify Vault plugin installed and economy provider active
- **Tokens:** Verify EIZZOs-Tokens plugin installed if `use-eizzos-tokens: true`
- Check `rewardLimit` hasn't been reached for player
- Enable `dialogue-to-console: true` to see reward execution logs

### NPCs not tracking players?
- Verify `trackingmode` is set to `STILL` or `FOLLOW` (not `NONE`)
- Check `trackingrange` is sufficient
- Ensure player is within 50-block cube (culling range)
- Use `/npc debug` to visualize tracking range with particles

---

## ⚠️ Important Notes

### Critical Gotchas
1. **Per-Player Health:** Killing an NPC for one player doesn't affect other players' views
2. **Packet-Based:** NPCs are not actual entities; `Bukkit.getEntity()` won't find them
3. **Spectator Exemption:** Spectator players never trigger tracking/combat
4. **Config Indentation:** YAML is extremely sensitive; use 2 spaces, never tabs
5. **Dialogue Timeouts:** 60-second timeout on `[choice]`/`[listen]` actions
6. **Token Dependency:** Soft dependency on EIZZOs-Tokens; logs warning if missing

### Range System
- **Culling Range:** 64×64×64 cube (hardcoded for show/hide)
- **Tracking Range:** Configurable per NPC (default 10 blocks)
- **Cube-Based:** All range checks use `Math.abs(dx) <= range` (not distance)

### Database
- SQLite default: `plugins/EIZZOs-NPCs/npcs.db`
- MySQL optional: Requires HikariCP configuration
- Automatic migrations: New columns added on version updates

---

## 📄 License

Part of the EIZZOs plugin suite for Minecraft multiplayer networks.

---

**Version:** 1.1.1
**Minecraft:** 1.21.1 (Paper)
**Author:** EIZZO
