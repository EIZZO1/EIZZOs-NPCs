package com.eizzo.npcs.gui;
import com.eizzo.npcs.EizzoNPCs;
import com.eizzo.npcs.managers.NPCManager;
import com.eizzo.npcs.models.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
public class NPCGUI implements Listener {
    private final EizzoNPCs plugin;
    private final NPCManager npcManager;
    private final Map<UUID, String> editingNpc = new HashMap<>();
    private final Map<UUID, String> chatInputAction = new HashMap<>();
    private final Map<UUID, Integer> editingCommandIndex = new HashMap<>();
    private final Map<UUID, String> editingDialogueNode = new HashMap<>();
    private final Map<UUID, Map<String, Object>> editingPropertyMap = new HashMap<>();
    private final List<String> popularSounds = Arrays.asList(
            "ui.button.click", "entity.player.levelup", "entity.experience_orb.pickup",
            "block.note_block.pling", "block.note_block.bit", "block.note_block.bell",
            "block.note_block.chime", "block.note_block.flute", "block.note_block.guitar",
            "block.note_block.xylophone", "entity.villager.ambient", "entity.villager.yes",
            "entity.villager.no", "entity.villager.trade", "entity.firework_rocket.launch",
            "entity.firework_rocket.twinkle", "block.anvil.land", "block.anvil.use",
            "block.chest.open", "block.chest.close", "entity.enderman.teleport",
            "entity.item.pickup", "entity.arrow.hit_player", "block.beacon.activate",
            "item.totem.use", "entity.generic.explode", "entity.lightning_bolt.thunder",
            "entity.wither.spawn", "entity.wither.death", "entity.dragon_fireball.explode",
            "block.respawn_anchor.charge", "block.respawn_anchor.deplete", "entity.player.hurt",
            "entity.player.death", "block.portal.travel", "block.portal.ambient"
    );
    public NPCGUI(EizzoNPCs plugin, NPCManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    public boolean isEditing(Player player, NPC npc) {
        String editingId = editingNpc.get(player.getUniqueId());
        return editingId != null && editingId.equals(npc.getId());
    }

    private void fill(Inventory inv) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
    }

    public void openList(Player player) {
        Collection<NPC> npcs = npcManager.getAllNPCs();
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("NPCs List"));
        fill(inv);
        int slot = 0;
        for (NPC npc : npcs) {
            if (slot >= 45) break;
            inv.setItem(slot++, createItem(Material.VILLAGER_SPAWN_EGG, "<yellow>" + npc.getId(), 
                "<gray>Name: <white>" + npc.getName(), "<gray>Type: <white>" + npc.getType(), "<gray>Click to Edit"));
        }
        inv.setItem(49, createItem(Material.NETHER_STAR, "<green>Create New NPC", "<gray>Places a default NPC at your location."));
        inv.setItem(53, createItem(Material.BARRIER, "<red>Close"));
        player.openInventory(inv);
        editingNpc.remove(player.getUniqueId());
    }

    public void openEditor(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Editing NPC: " + npc.getId()));
        fill(inv);
        inv.setItem(10, createItem(Material.NAME_TAG, "<yellow>Appearance", "<gray>Change name, skin, type, etc."));
        inv.setItem(11, createItem(Material.COMPASS, "<yellow>Behavior", "<gray>Tracking, flying, return to spawn."));
        inv.setItem(12, createItem(Material.COMMAND_BLOCK, "<yellow>Interactions", "<gray>Commands and execution modes."));
        inv.setItem(13, createItem(Material.ANVIL, "<yellow>Physics", "<gray>Collision and physical settings."));
        inv.setItem(14, createItem(Material.GOLDEN_APPLE, "<gold>Combat & Health", "<gray>Godmode, max health, respawns."));
        inv.setItem(15, createItem(Material.CHEST, "<yellow>Equipment", "<gray>Armor and hand items."));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back to List"));
        inv.setItem(26, createItem(Material.BARRIER, "<red>Delete NPC", "<gray>Remove permanently."));
        player.openInventory(inv);
        editingNpc.put(player.getUniqueId(), npc.getId());
    }

    public void openAppearance(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Appearance: " + npc.getId()));
        fill(inv);
        inv.setItem(10, createItem(Material.OAK_SIGN, "<yellow>Internal ID", "<gray>Current: " + npc.getId(), "<gray>Click to change."));
        inv.setItem(11, createItem(Material.NAME_TAG, "<yellow>Name", "<gray>Current: " + npc.getName(), "<gray>Click to change."));
        inv.setItem(12, createItem(Material.ZOMBIE_HEAD, "<yellow>Type", "<gray>Current: " + npc.getType(), "<gray>Click to cycle."));
        inv.setItem(13, createItem(Material.PLAYER_HEAD, "<yellow>Skin", "<gray>Current: " + (npc.getSkinName() == null ? "None" : npc.getSkinName()), "<gray>Click to set."));
        inv.setItem(14, createItem(Material.LEATHER_CHESTPLATE, "<yellow>Cape", "<gray>Visible: " + (npc.isShowCape() ? "<green>YES" : "<red>NO")));
        inv.setItem(15, createItem(Material.PAPER, "<yellow>Nametag", "<gray>Visible: " + (npc.isNametagVisible() ? "<green>YES" : "<red>NO")));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openBehavior(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Behavior: " + npc.getId()));
        fill(inv);
        inv.setItem(10, createItem(Material.ENDER_EYE, "<yellow>Tracking Mode", "<gray>Mode: <white>" + npc.getTrackingMode().name()));
        inv.setItem(11, createItem(Material.SPYGLASS, "<yellow>Tracking Range", "<gray>Range: <white>" + (int)npc.getTrackingRange()));
        inv.setItem(12, createItem(Material.FEATHER, "<yellow>Flying", "<gray>Enabled: " + (npc.isFlying() ? "<green>YES" : "<red>NO")));
        inv.setItem(13, createItem(Material.LEAD, "<yellow>Return to Spawn", "<gray>Enabled: " + (npc.isReturnToSpawn() ? "<green>YES" : "<red>NO")));
        inv.setItem(14, createItem(Material.IRON_SWORD, "<red>Hostile Mode", "<gray>Chases and attacks players.", "<gray>Enabled: " + (npc.isHostile() ? "<green>YES" : "<red>NO")));
        inv.setItem(15, createItem(Material.ENDER_PEARL, "<yellow>Teleport", "<yellow>Left-Click: <white>TP to you", "<aqua>Right-Click: <white>Manual Entry (Coords)"));
        if (plugin.getConfig().getBoolean("rewards.enabled", true)) {
            inv.setItem(16, createItem(Material.GOLD_INGOT, "<yellow>NPC Rewards", "<gray>Vault and Token rewards on death."));
        }
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openCombatSettings(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Combat & Health: " + npc.getId()));
        fill(inv);
        inv.setItem(10, createItem(Material.ENCHANTED_GOLDEN_APPLE, "<gold>God Mode", "<gray>Enabled: " + (npc.isGodMode() ? "<green>YES" : "<red>NO"), "<gray>Attacks are ignored."));
        inv.setItem(12, createItem(Material.REDSTONE, "<red>Max Health", "<gray>Current: <white>" + (int)npc.getMaxHealth(), "<gray>Click to change."));
        inv.setItem(14, createItem(Material.BEACON, "<aqua>Health Bar", "<gray>Visible: " + (npc.isShowHealthBar() ? "<green>YES" : "<red>NO")));
        inv.setItem(16, createItem(Material.CLOCK, "<yellow>Respawn Delay", "<gray>Seconds: <white>" + npc.getRespawnDelay(), "<gray>Click to change."));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openRewardSettings(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("NPC Rewards: " + npc.getId()));
        fill(inv);
        
        boolean useVault = plugin.getConfig().getBoolean("rewards.use-vault", true);
        boolean useTokens = plugin.getConfig().getBoolean("rewards.use-eizzos-tokens", false);
        plugin.getLogger().info("[Debug] Rewards Menu: use-vault=" + useVault + ", use-tokens=" + useTokens);

        if (useVault) {
            inv.setItem(10, createItem(Material.GOLD_NUGGET, "<green>Vault Reward", "<gray>Current: <white>$" + npc.getVaultReward(), "<gray>Click to change."));
        } else {
            inv.setItem(10, createItem(Material.BARRIER, "<red>Vault Disabled", "<gray>Enable in config.yml."));
        }
        if (useTokens) {
            inv.setItem(13, createItem(Material.SUNFLOWER, "<gold>Token Reward", "<gray>Current: <white>" + npc.getTokenReward(), "<gray>Click to change."));
            inv.setItem(14, createItem(Material.NAME_TAG, "<yellow>Token ID", "<gray>Current: <white>" + npc.getRewardTokenId(), "<gray>Click to change."));
        } else {
            inv.setItem(13, createItem(Material.BARRIER, "<red>Tokens Disabled", "<gray>Enable in config.yml."));
        }
        inv.setItem(16, createItem(Material.REPEATER, "<yellow>Max Reward Count", "<gray>Current Limit: <white>" + (npc.getRewardLimit() == 0 ? "Unlimited" : npc.getRewardLimit()), "<gray>Click to change."));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openInteractions(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Interactions: " + npc.getId()));
        fill(inv);
        inv.setItem(11, createItem(Material.COMMAND_BLOCK, "<yellow>Edit Commands", "<gray>Current count: " + npc.getCommands().size(), "<gray>Click to view/add/remove."));
        inv.setItem(13, createItem(Material.REDSTONE_TORCH, "<yellow>Run Mode", "<gray>Op: " + (npc.isRunAsOp() ? "<green>YES" : "<red>NO"), "<gray>Console: " + (npc.isRunAsConsole() ? "<green>YES" : "<red>NO")));
        inv.setItem(15, createItem(Material.WRITABLE_BOOK, "<yellow>Manage Dialogue", "<gray>Create branching conversations", "<gray>with clickable reactions."));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openCommandList(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Commands: " + npc.getId()));
        fill(inv);
        List<String> cmds = npc.getCommands();
        for (int i = 0; i < Math.min(45, cmds.size()); i++) {
            inv.setItem(i, createItem(Material.PAPER, "<white>Command #" + (i + 1), "<gray>" + cmds.get(i), "<yellow>Left-Click: <white>Change Type", "<red>Right-Click: <white>Delete"));
        }
        inv.setItem(47, createItem(Material.NETHER_STAR, "<green>Add Normal Command", "<gray>Click to type a command."));
        inv.setItem(48, createItem(Material.CLOCK, "<aqua>Add Wait (Sleep)", "<gray>Click to type seconds (0.01 - 60)."));
        inv.setItem(49, createItem(Material.JUKEBOX, "<yellow>Add Sound Effect", "<gray>Click to browse and add a sound."));
        inv.setItem(50, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openCommandTypeSelector(Player player, NPC npc, int index) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Edit Command Type: #" + (index + 1)));
        fill(inv);
        inv.setItem(11, createItem(Material.COMMAND_BLOCK, "<green>Change to Command", "<gray>Enter a new command string."));
        inv.setItem(13, createItem(Material.CLOCK, "<aqua>Change to Delay", "<gray>Enter a new wait time in seconds."));
        inv.setItem(15, createItem(Material.JUKEBOX, "<yellow>Change to Sound", "<gray>Choose a new sound effect."));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back to List"));
        player.openInventory(inv);
        editingCommandIndex.put(player.getUniqueId(), index);
    }

    public void openDialogueStepTypeSelector(Player player, NPC npc, String nodeName, int index) {
        Inventory inv = Bukkit.createInventory(null, 45, Component.text("Edit Step Type: #" + (index + 1)));
        fill(inv);
        // Row 1: Interactions
        inv.setItem(10, createItem(Material.WRITABLE_BOOK, "<green>Change to Message", "<gray>Click to type a new chat message."));
        inv.setItem(12, createItem(Material.COMMAND_BLOCK, "<green>Change to Command", "<gray>Click to type a server command."));
        inv.setItem(14, createItem(Material.HOPPER, "<gold>Change to Choice", "<gray>Click to type branching options."));
        inv.setItem(16, createItem(Material.JUKEBOX, "<yellow>Change to Sound", "<gray>Browse the sound library."));
        // Row 2: Timing & State
        inv.setItem(20, createItem(Material.CLOCK, "<aqua>Change to Wait", "<gray>Click to type wait time (0.01-60s)."));
        inv.setItem(22, createItem(Material.REPEATER, "<light_purple>Change to Property Set", "<gray>Set temp property overrides."));
        inv.setItem(24, createItem(Material.COMPASS, "<aqua>Change to Location Listen", "<gray>Pause until coord reached."));

        // Row 3: World & Special
        inv.setItem(29, createItem(Material.GOLD_NUGGET, "<green>Change to Reward", "<gray>Trigger configured rewards."));
        inv.setItem(31, createItem(Material.BEACON, "<yellow>Change to Set NPC Home", "<gray>Sets home to current pos."));
        inv.setItem(33, createItem(Material.RABBIT_FOOT, "<light_purple>Change to Jump", "<gray>Make the NPC jump once."));
        inv.setItem(40, createItem(Material.ARROW, "<gray>Back to Node Editor"));
        player.openInventory(inv);
        editingCommandIndex.put(player.getUniqueId(), index);
        editingDialogueNode.put(player.getUniqueId(), nodeName);
    }

    public void openPropertyLibrary(Player player, NPC npc, String nodeName) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Property Presets: " + nodeName));
        fill(inv);
        Map<String, Object> currentEdits = editingPropertyMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        // --- Row 1: Appearance ---
        inv.setItem(10, createPropertyItem(Material.ZOMBIE_HEAD, "Type", "type", npc.getType(), currentEdits.getOrDefault("type", npc.getType())));
        inv.setItem(11, createPropertyItem(Material.PLAYER_HEAD, "Skin", "skin", npc.getSkinName(), currentEdits.getOrDefault("skin", npc.getSkinName())));
        inv.setItem(12, createPropertyItem(Material.LEATHER_CHESTPLATE, "Cape", "cape", npc.isShowCape(), currentEdits.getOrDefault("cape", npc.isShowCape())));
        inv.setItem(14, createPropertyItem(Material.PAPER, "Nametag", "nametag", npc.isNametagVisible(), currentEdits.getOrDefault("nametag", npc.isNametagVisible())));
        // --- Row 2: Behavior ---
        inv.setItem(19, createPropertyItem(Material.ENDER_EYE, "Tracking Mode", "trackingmode", npc.getTrackingMode(), currentEdits.getOrDefault("trackingmode", npc.getTrackingMode())));
        inv.setItem(21, createPropertyItem(Material.FEATHER, "Flying", "flying", npc.isFlying(), currentEdits.getOrDefault("flying", npc.isFlying())));
        inv.setItem(23, createPropertyItem(Material.LEAD, "ReturnToSpawn", "returntospawn", npc.isReturnToSpawn(), currentEdits.getOrDefault("returntospawn", npc.isReturnToSpawn())));
        inv.setItem(25, createPropertyItem(Material.IRON_SWORD, "Hostility", "hostile", npc.isHostile(), currentEdits.getOrDefault("hostile", npc.isHostile())));
        // --- Row 3: Physics ---
        inv.setItem(30, createPropertyItem(Material.ANVIL, "Pushing Player (Collisions logic)", "collidable", npc.isCollidable(), currentEdits.getOrDefault("collidable", npc.isCollidable())));
        inv.setItem(32, createPropertyItem(Material.SLIME_BALL, "Pushing NPC (Collisions logic)", "npccollision", npc.isNpcCollision(), currentEdits.getOrDefault("npccollision", npc.isNpcCollision())));
        inv.setItem(48, createItem(Material.WRITABLE_BOOK, "<aqua>Manual Entry", "<gray>Click to type custom property set"));
        inv.setItem(49, createItem(Material.GREEN_STAINED_GLASS_PANE, "<green><b>DONE</b>", "<gray>Click to save all selected changes", "<gray>as a single [set] action."));
        inv.setItem(50, createItem(Material.BARRIER, "<red>Cancel"));
        player.openInventory(inv);
        editingDialogueNode.put(player.getUniqueId(), nodeName);
    }

    private ItemStack createPropertyItem(Material mat, String title, String propName, Object normal, Object currentEdit) {
        String normalStr = formatVal(normal);
        String currentStr = formatVal(currentEdit);
        boolean changed = !normal.toString().equals(currentEdit.toString());
        return createItem(mat, (changed ? "<green>" : "<yellow>") + title, 
            "<gray>Property: <white>" + propName,
            "<gray>Normal: " + normalStr,
            "<gray>Selected: " + currentStr,
            "",
            "<yellow>Click to toggle/cycle state");
    }

    private String formatVal(Object val) {
        if (val instanceof Boolean) return (boolean)val ? "<green>ENABLED" : "<red>DISABLED";
        return "<white>" + val.toString();
    }

    public void openSoundLibrary(Player player, NPC npc, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Sound Library: " + npc.getId() + " (P" + (page + 1) + ")"));
        fill(inv);
        int start = page * 28;
        int slot = 10;
        for (int i = start; i < Math.min(start + 28, popularSounds.size()); i++) {
            while (slot % 9 == 0 || slot % 9 == 8) slot++;
            String s = popularSounds.get(i);
            inv.setItem(slot++, createItem(Material.BOOK, "<yellow>" + s, 
                "<gray>Right-Click: <white>Preview", 
                "<gray>Left-Click: <white>Select Sound"));
        }
        if (page > 0) inv.setItem(45, createItem(Material.ARROW, "<yellow>Previous Page"));
        if (start + 28 < popularSounds.size()) inv.setItem(53, createItem(Material.ARROW, "<yellow>Next Page"));
        inv.setItem(48, createItem(Material.WRITABLE_BOOK, "<aqua>Manual Entry", "<gray>Click to type ANY sound key", "<gray>using tab completion in chat."));
        inv.setItem(49, createItem(Material.BARRIER, "<red>Back"));
        player.openInventory(inv);
    }

    public void openPhysics(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Physics: " + npc.getId()));
        fill(inv);
        inv.setItem(12, createItem(Material.ANVIL, "<yellow>Pushing Player (Collisions logic)", "<gray>Push Players: " + (npc.isCollidable() ? "<green>YES" : "<red>NO")));
        inv.setItem(14, createItem(Material.SLIME_BALL, "<yellow>Pushing NPC (Collisions logic)", "<gray>Push NPCs: " + (npc.isNpcCollision() ? "<green>YES" : "<red>NO")));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openEquipmentEditor(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Equipment: " + npc.getId()));
        fill(inv);
        inv.setItem(13, getEquip(npc, EquipmentSlot.HEAD, Material.NETHERITE_HELMET));
        inv.setItem(21, getEquip(npc, EquipmentSlot.HAND, Material.NETHERITE_SWORD));
        inv.setItem(22, getEquip(npc, EquipmentSlot.CHEST, Material.NETHERITE_CHESTPLATE));
        inv.setItem(23, getEquip(npc, EquipmentSlot.OFF_HAND, Material.SHIELD));
        inv.setItem(31, getEquip(npc, EquipmentSlot.LEGS, Material.NETHERITE_LEGGINGS));
        inv.setItem(40, getEquip(npc, EquipmentSlot.FEET, Material.NETHERITE_BOOTS));
        inv.setItem(49, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openDeleteConfirmation(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Delete NPC: " + npc.getId() + "?"));
        fill(inv);
        inv.setItem(11, createItem(Material.RED_STAINED_GLASS_PANE, "<red>CONFIRM DELETE", "<gray>This action is permanent!"));
        inv.setItem(15, createItem(Material.GREEN_STAINED_GLASS_PANE, "<green>CANCEL", "<gray>Return to editor."));
        player.openInventory(inv);
    }

    public void openDialogueManager(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Dialogues: " + npc.getId()));
        fill(inv);
        List<String> nodeNames = new ArrayList<>(npc.getDialogues().keySet());
        Collections.sort(nodeNames);
        for (int i = 0; i < Math.min(45, nodeNames.size()); i++) {
            String name = nodeNames.get(i);
            inv.setItem(i, createItem(Material.BOOK, "<yellow>Node: <white>" + name, 
                "<gray>Steps: <white>" + npc.getDialogues().get(name).size(),
                "<yellow>Left-Click: <white>Edit Node",
                "<red>Right-Click: <white>Delete Node"));
        }
        inv.setItem(48, createItem(Material.WRITABLE_BOOK, "<yellow>Dialogue Mode", "<gray>Only Play Once: " + (npc.isDialogueOnce() ? "<green>YES" : "<red>NO"), "<gray>If YES, it cycles through", "<gray>start, start1, start2..."));
        inv.setItem(49, createItem(Material.NETHER_STAR, "<green>Create New Node", "<gray>Add a new dialogue scene."));
        inv.setItem(53, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    public void openDialogueNodeEditor(Player player, NPC npc, String nodeName) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Dialogue Node: " + nodeName));
        fill(inv);
        List<String> steps = npc.getDialogues().getOrDefault(nodeName, new ArrayList<>());
        for (int i = 0; i < Math.min(45, steps.size()); i++) {
            inv.setItem(i, createItem(Material.PAPER, "<white>Step #" + (i + 1), "<gray>" + steps.get(i), 
                "<yellow>Left-Click: <white>Edit Type", "<red>Right-Click: <white>Delete"));
        }
        inv.setItem(49, createItem(Material.NETHER_STAR, "<green><b>Insert New Action</b>", "<gray>Click to choose a type of action", "<gray>to add to this sequence."));
        inv.setItem(53, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
        editingNpc.put(player.getUniqueId(), npc.getId());
        chatInputAction.put(player.getUniqueId(), "dialog_node:" + nodeName);
    }

    public void openActionTypeSelector(Player player, NPC npc, String nodeName) {
        Inventory inv = Bukkit.createInventory(null, 45, Component.text("Insert Action: " + nodeName));
        fill(inv);
        // Row 1: Interactions
        inv.setItem(10, createItem(Material.WRITABLE_BOOK, "<green>Insert Message", "<gray>Send chat to player."));
        inv.setItem(12, createItem(Material.COMMAND_BLOCK, "<green>Insert Command", "<gray>Run server command."));
        inv.setItem(14, createItem(Material.HOPPER, "<gold>Insert Choice", "<gray>Add clickable reactions."));
        inv.setItem(16, createItem(Material.JUKEBOX, "<yellow>Insert Sound", "<gray>Play an effect."));
        // Row 2: Timing & State
        inv.setItem(20, createItem(Material.CLOCK, "<aqua>Insert Wait", "<gray>Pause for X seconds."));
        inv.setItem(22, createItem(Material.REPEATER, "<light_purple>Insert Property Set", "<gray>Set temp property overrides."));
        inv.setItem(24, createItem(Material.COMPASS, "<aqua>Insert Location Listen", "<gray>Pause until coord reached."));

        // Row 3: World & Special
        inv.setItem(29, createItem(Material.GOLD_NUGGET, "<green>Insert Reward", "<gray>Give configured rewards now."));
        inv.setItem(31, createItem(Material.BEACON, "<yellow>Insert Set NPC Home", "<gray>Sets home to current pos."));
        inv.setItem(33, createItem(Material.RABBIT_FOOT, "<light_purple>Insert Jump", "<gray>Make the NPC jump once."));
        inv.setItem(40, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
    }

    private ItemStack getEquip(NPC npc, EquipmentSlot slot, Material ph) {
        ItemStack item = npc.getEquipment().get(slot);
        return (item == null || item.getType().isAir()) ? createItem(ph, "<gray>Empty " + slot.name()) : item.clone();
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("<!i>" + name));
        if (lore.length > 0) {
            List<Component> l = new ArrayList<>();
            for (String line : lore) l.add(MiniMessage.miniMessage().deserialize("<!i>" + line));
            meta.lore(l);
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        int slot = event.getRawSlot();
        if (title.equals("NPCs List")) {
            event.setCancelled(true);
            if (slot == 49) {
                int i = 1;
                while (npcManager.getNPC("setid" + i) != null) i++;
                npcManager.createNPC("setid" + i, "setname" + i, EntityType.VILLAGER, player.getLocation());
                com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<green>Created NPC with ID: <white>setid" + i);
                openList(player);
                return;
            }
            if (slot == 53) { player.closeInventory(); return; }
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.VILLAGER_SPAWN_EGG) {
                String id = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName()).trim();
                NPC npc = npcManager.getNPC(id);
                if (npc != null) openEditor(player, npc);
            }
        } else if (title.startsWith("Editing NPC: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 10: openAppearance(player, npc); break;
                case 11: openBehavior(player, npc); break;
                case 12: openInteractions(player, npc); break;
                case 13: openPhysics(player, npc); break;
                case 14: openCombatSettings(player, npc); break;
                case 15: openEquipmentEditor(player, npc); break;
                case 22: openList(player); break;
                case 26: openDeleteConfirmation(player, npc); break;
            }
        } else if (title.startsWith("Delete NPC: ")) {
            event.setCancelled(true);
            String id = title.substring(title.indexOf(": ") + 2, title.lastIndexOf("?"));
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 11:
                    npcManager.deleteNPC(npc.getId());
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<red>NPC deleted successfully.");
                    openList(player);
                    break;
                case 15:
                    openEditor(player, npc);
                    break;
            }
        } else if (title.startsWith("Behavior: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 10: npc.setTrackingMode(NPC.TrackingMode.values()[(npc.getTrackingMode().ordinal() + 1) % NPC.TrackingMode.values().length]); openBehavior(player, npc); break;
                case 11: npc.setTrackingRange(npc.getTrackingRange() >= 50 ? 5 : npc.getTrackingRange() + 5); openBehavior(player, npc); break;
                case 12: npc.setFlying(!npc.isFlying()); npcManager.spawnNPC(npc); openBehavior(player, npc); break;
                case 13: npc.setReturnToSpawn(!npc.isReturnToSpawn()); openBehavior(player, npc); break;
                case 14: npc.setHostile(!npc.isHostile()); openBehavior(player, npc); break;
                case 15: 
                    if (event.isLeftClick()) {
                        npc.setLocation(player.getLocation()); 
                        npcManager.spawnNPC(npc); 
                        openBehavior(player, npc); 
                    } else if (event.isRightClick()) {
                        player.closeInventory();
                        chatInputAction.put(player.getUniqueId(), "manual_tp");
                        com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type coordinates: <white>world x y z yaw pitch");
                    }
                    break;
                case 16: if (plugin.getConfig().getBoolean("rewards.enabled", true)) openRewardSettings(player, npc); break;
                case 22: openEditor(player, npc); break;
            }
            npcManager.saveNPCs();
        } else if (title.startsWith("Combat & Health: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 10: npc.setGodMode(!npc.isGodMode()); openCombatSettings(player, npc); break;
                case 12: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "max_health"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type max health (1 - 1000)."); break;
                case 14: 
                    npc.setShowHealthBar(!npc.isShowHealthBar()); 
                    npcManager.spawnNPC(npc); 
                    openCombatSettings(player, npc); 
                    break;
                case 16: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "respawn_delay"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type respawn delay in seconds (1 - 3600)."); break;
                case 22: openEditor(player, npc); break;
            }
            npcManager.saveNPC(npc);
        } else if (title.startsWith("NPC Rewards: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 10: 
                    if (plugin.getConfig().getBoolean("rewards.use-vault", true)) {
                        player.closeInventory(); chatInputAction.put(player.getUniqueId(), "reward_vault"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type Vault reward amount.");
                    }
                    break;
                case 13: 
                    if (plugin.getConfig().getBoolean("rewards.use-eizzos-tokens", false)) {
                        player.closeInventory(); chatInputAction.put(player.getUniqueId(), "reward_token_amount"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type token reward amount.");
                    }
                    break;
                case 14: 
                    if (plugin.getConfig().getBoolean("rewards.use-eizzos-tokens", false)) {
                        player.closeInventory(); chatInputAction.put(player.getUniqueId(), "reward_token_id"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type Token ID.");
                    }
                    break;
                case 16: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "reward_limit"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type max reward claim count (0 for unlimited)."); break;
                case 22: openCombatSettings(player, npc); break;
            }
            npcManager.saveNPC(npc);
        } else if (title.startsWith("Appearance: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 10: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "rename_id"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new internal ID in chat."); break;
                case 11: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "name"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new name in chat."); break;
                case 12: npc.setType(cycleType(npc.getType())); npcManager.spawnNPC(npc); openAppearance(player, npc); break;
                case 13: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "skin"); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type player name or file name in chat."); break;
                case 14: npc.setShowCape(!npc.isShowCape()); npcManager.spawnNPC(npc); openAppearance(player, npc); break;
                case 15: npc.setNametagVisible(!npc.isNametagVisible()); npcManager.spawnNPC(npc); openAppearance(player, npc); break;
                case 22: openEditor(player, npc); break;
            }
            npcManager.saveNPCs();
        } else if (title.startsWith("Interactions: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 11: openCommandList(player, npc); break;
                case 13:
                    if (npc.isRunAsOp()) { npc.setRunAsOp(false); npc.setRunAsConsole(true); }
                    else if (npc.isRunAsConsole()) npc.setRunAsConsole(false);
                    else npc.setRunAsOp(true);
                    openInteractions(player, npc);
                    break;
                case 15: openDialogueManager(player, npc); break;
                case 22: openEditor(player, npc); break;
            }
            npcManager.saveNPCs();
        } else if (title.startsWith("Dialogues: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            if (slot == 53) { openInteractions(player, npc); return; }
            if (slot == 48) { npc.setDialogueOnce(!npc.isDialogueOnce()); openDialogueManager(player, npc); npcManager.saveNPC(npc); return; }
            if (slot == 49) {
                player.closeInventory();
                chatInputAction.put(player.getUniqueId(), "create_node");
                com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new Dialogue Node name (e.g. 'start', 'yes').");
                return;
            }
            if (slot < 45) {
                ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() == Material.BOOK) {
                    String nodeName = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName()).replace("Node: ", "").trim();
                    if (event.isLeftClick()) openDialogueNodeEditor(player, npc, nodeName);
                    else if (event.isRightClick()) {
                        npc.getDialogues().remove(nodeName);
                        openDialogueManager(player, npc);
                        npcManager.saveNPCs();
                    }
                }
            }
        } else if (title.startsWith("Dialogue Node: ")) {
            event.setCancelled(true);
            String nodeName = title.split(": ")[1];
            NPC npc = npcManager.getNPC(editingNpc.get(player.getUniqueId()));
            if (npc == null) return;
            if (slot == 53) { openDialogueManager(player, npc); return; }
            List<String> steps = npc.getDialogues().get(nodeName);
            if (slot < 45 && slot < steps.size()) {
                if (event.isRightClick()) {
                    steps.remove(slot);
                    openDialogueNodeEditor(player, npc, nodeName);
                } else if (event.isLeftClick()) {
                    openDialogueStepTypeSelector(player, npc, nodeName, slot);
                }
            } else if (slot == 49) {
                openActionTypeSelector(player, npc, nodeName);
            }
            npcManager.saveNPCs();
        } else if (title.startsWith("Insert Action: ")) {
            event.setCancelled(true);
            String nodeName = title.split(": ")[1];
            NPC npc = npcManager.getNPC(editingNpc.get(player.getUniqueId()));
            if (npc == null) return;
            if (slot == 40) { openDialogueNodeEditor(player, npc, nodeName); return; }
            switch (slot) {
                case 10: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_msg:" + nodeName); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type message to send."); break;
                case 12: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_diag_cmd:" + nodeName); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type command to run."); break;
                case 14: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_choice:" + nodeName); 
                         com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type choices format: <white>Label1=NodeName | Label2=NodeName"); break;
                case 16: openSoundLibrary(player, npc, 0); break;
                case 20: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_diag_wait:" + nodeName); com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type seconds to wait (0.01 - 60)."); break;
                case 29:
                    npc.getDialogues().get(nodeName).add("[reward]");
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<green>Added [reward] action.");
                    openDialogueNodeEditor(player, npc, nodeName);
                    break;
                case 22: editingPropertyMap.remove(player.getUniqueId()); openPropertyLibrary(player, npc, nodeName); break;
                case 24: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_diag_listen:" + nodeName);
                         com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type coordinates format: <white>X Y Z <yellow>or <white>World X Y Z"); break;
                case 31: 
                    Location loc = player.getLocation();
                    String locStr = loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getYaw() + " " + loc.getPitch();
                    npc.getDialogues().get(nodeName).add("[home] " + locStr); 
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<green>Added [home] action at your current location.");
                    openDialogueNodeEditor(player, npc, nodeName);
                    break;
                case 33:
                    npc.getDialogues().get(nodeName).add("[jump]");
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<green>Added [jump] action.");
                    openDialogueNodeEditor(player, npc, nodeName);
                    break;
            }
            npcManager.saveNPCs();
        } else if (title.startsWith("Property Presets: ")) {
            event.setCancelled(true);
            String nodeName = title.split(": ")[1];
            NPC npc = npcManager.getNPC(editingNpc.get(player.getUniqueId()));
            if (npc == null) return;
            Map<String, Object> edits = editingPropertyMap.get(player.getUniqueId());
            if (slot == 50) { openDialogueNodeEditor(player, npc, nodeName); return; }
            if (slot == 49) {
                if (edits.isEmpty()) { openDialogueNodeEditor(player, npc, nodeName); return; }
                StringBuilder sb = new StringBuilder("[set] ");
                boolean first = true;
                for (Map.Entry<String, Object> entry : edits.entrySet()) {
                    if (!first) sb.append(";");
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                    first = false;
                }
                String action = chatInputAction.getOrDefault(player.getUniqueId(), "");
                if (action.equals("edit_diag_set")) {
                    int index = editingCommandIndex.getOrDefault(player.getUniqueId(), -1);
                    if (index != -1) {
                        npc.getDialogues().get(nodeName).set(index, sb.toString());
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Updated multi-set action: <white>" + sb.toString()));
                    }
                } else {
                    npc.getDialogues().get(nodeName).add(sb.toString());
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Added multi-set action: <white>" + sb.toString()));
                }
                npcManager.saveNPCs();
                openDialogueNodeEditor(player, npc, nodeName);
                return;
            }
            if (slot == 48) {
                player.closeInventory();
                chatInputAction.put(player.getUniqueId(), "add_diag_set:" + nodeName);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type property set format: <white>key1=val1;key2=val2"));
                return;
            }
            switch (slot) {
                case 10: edits.put("type", cycleType((EntityType)edits.getOrDefault("type", npc.getType()))); break;
                case 12: edits.put("cape", !(boolean)edits.getOrDefault("cape", npc.isShowCape())); break;
                case 14: edits.put("nametag", !(boolean)edits.getOrDefault("nametag", npc.isNametagVisible())); break;
                case 19: 
                    NPC.TrackingMode current = (NPC.TrackingMode)edits.getOrDefault("trackingmode", npc.getTrackingMode());
                    edits.put("trackingmode", NPC.TrackingMode.values()[(current.ordinal() + 1) % NPC.TrackingMode.values().length]); 
                    break;
                case 21: edits.put("flying", !(boolean)edits.getOrDefault("flying", npc.isFlying())); break;
                case 23: edits.put("returntospawn", !(boolean)edits.getOrDefault("returntospawn", npc.isReturnToSpawn())); break;
                case 25: edits.put("hostile", !(boolean)edits.getOrDefault("hostile", npc.isHostile())); break;
                case 30: edits.put("collidable", !(boolean)edits.getOrDefault("collidable", npc.isCollidable())); break;
                case 32: edits.put("npccollision", !(boolean)edits.getOrDefault("npccollision", npc.isNpcCollision())); break;
            }
            openPropertyLibrary(player, npc, nodeName);
        } else if (title.startsWith("Sound Library: ")) {
            event.setCancelled(true);
            String id = title.substring(title.indexOf(": ") + 2, title.lastIndexOf(" (P"));
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            int currentPage = 0;
            try {
                String pStr = title.substring(title.lastIndexOf("(P") + 2, title.lastIndexOf(")"));
                currentPage = Integer.parseInt(pStr) - 1;
            } catch (Exception ignored) {}
            if (slot == 49) { 
                if (editingCommandIndex.containsKey(player.getUniqueId())) {
                    openCommandTypeSelector(player, npc, editingCommandIndex.get(player.getUniqueId()));
                } else {
                    openCommandList(player, npc); 
                }
                return; 
            }
            if (slot == 45 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) { openSoundLibrary(player, npc, currentPage - 1); return; }
            if (slot == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) { openSoundLibrary(player, npc, currentPage + 1); return; }
            if (slot == 48) {
                player.closeInventory();
                Component msg = MiniMessage.miniMessage().deserialize("<yellow>Click the command below to add a custom sound with tab completion:\n")
                        .append(Component.text("/npc addcmd " + npc.getId() + " [sound] ")
                                .color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.suggestCommand("/npc addcmd " + npc.getId() + " [sound] ")));
                player.sendMessage(msg);
                return;
            }
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.BOOK) {
                String soundName = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName()).trim();
                if (event.isRightClick()) {
                    player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);
                } else if (event.isLeftClick()) {
                    if (chatInputAction.getOrDefault(player.getUniqueId(), "").startsWith("dialog_node:")) {
                        String node = chatInputAction.get(player.getUniqueId()).split(":")[1];
                        npc.getDialogues().get(node).add("[sound] " + soundName);
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Added [sound] <white>" + soundName + " <green>to node <white>" + node));
                        npcManager.saveNPCs();
                        final NPC fNpc = npc;
                        Bukkit.getScheduler().runTask(plugin, () -> openDialogueNodeEditor(player, fNpc, node));
                        return;
                    }
                    if (editingCommandIndex.containsKey(player.getUniqueId())) {
                        int index = editingCommandIndex.remove(player.getUniqueId());
                        npc.getCommands().set(index, "[sound] " + soundName);
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Updated command #" + (index + 1) + " to [sound] <white>" + soundName));
                    } else {
                        npc.getCommands().add("[sound] " + soundName);
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Added [sound] <white>" + soundName + " <green>to commands."));
                    }
                    npcManager.saveNPCs();
                    final NPC finalNpc = npc;
                    Bukkit.getScheduler().runTask(plugin, () -> openCommandList(player, finalNpc));
                }
            }
        } else if (title.startsWith("Commands: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            if (slot == 50) { openInteractions(player, npc); return; }
            if (slot < 45) {
                if (slot < npc.getCommands().size()) {
                    if (event.isRightClick()) {
                        npc.getCommands().remove(slot);
                        openCommandList(player, npc);
                    } else if (event.isLeftClick()) {
                        openCommandTypeSelector(player, npc, slot);
                    }
                }
            } else if (slot == 47) {
                player.closeInventory();
                chatInputAction.put(player.getUniqueId(), "cmd");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type command in chat."));
                return;
            } else if (slot == 48) {
                player.closeInventory();
                chatInputAction.put(player.getUniqueId(), "wait");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type seconds to sleep (0.01 - 10)."));
                return;
            } else if (slot == 49) {
                editingCommandIndex.remove(player.getUniqueId());
                openSoundLibrary(player, npc, 0);
                return;
            }
            npcManager.saveNPCs();
        } else if (title.startsWith("Edit Step Type: #")) {
            event.setCancelled(true);
            String npcId = editingNpc.get(player.getUniqueId());
            NPC npc = npcManager.getNPC(npcId);
            int index = editingCommandIndex.getOrDefault(player.getUniqueId(), -1);
            String nodeName = editingDialogueNode.get(player.getUniqueId());
            if (npc == null || index == -1 || nodeName == null) return;
            if (slot == 40) { openDialogueNodeEditor(player, npc, nodeName); return; }
            switch (slot) {
                case 10: 
                    player.closeInventory(); 
                    chatInputAction.put(player.getUniqueId(), "edit_diag_msg"); 
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new message for step #" + (index + 1) + "."); 
                    break;
                case 12: 
                    player.closeInventory(); 
                    chatInputAction.put(player.getUniqueId(), "edit_diag_cmd"); 
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new command for step #" + (index + 1) + "."); 
                    break;
                case 14:
                    player.closeInventory();
                    chatInputAction.put(player.getUniqueId(), "edit_diag_choice");
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new choices for step #" + (index + 1) + ".");
                    break;
                case 16: 
                    openSoundLibrary(player, npc, 0); 
                    break;
                case 20: 
                    player.closeInventory(); 
                    chatInputAction.put(player.getUniqueId(), "edit_diag_wait"); 
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new wait time for step #" + (index + 1) + "."); 
                    break;
                case 29:
                    npc.getDialogues().get(nodeName).set(index, "[reward]");
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<green>Changed step #" + (index + 1) + " to [reward].");
                    openDialogueNodeEditor(player, npc, nodeName);
                    break;
                case 22:
                    editingPropertyMap.remove(player.getUniqueId());
                    openPropertyLibrary(player, npc, nodeName);
                    // Note: onChat/DONE handles adding, but here we are EDITING.
                    // To simplify, we'll let it ADD a new one or the user can delete old one.
                    // Or we could flag it as 'editing_existing_step'.
                    chatInputAction.put(player.getUniqueId(), "edit_diag_set");
                    break;
                case 24:
                    player.closeInventory();
                    chatInputAction.put(player.getUniqueId(), "edit_diag_listen");
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new coordinates for step #" + (index + 1) + ".");
                    break;
                case 31:
                    Location editLoc = player.getLocation();
                    String editLocStr = editLoc.getWorld().getName() + " " + editLoc.getX() + " " + editLoc.getY() + " " + editLoc.getZ() + " " + editLoc.getYaw() + " " + editLoc.getPitch();
                    npc.getDialogues().get(nodeName).set(index, "[home] " + editLocStr);
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<green>Changed step #" + (index + 1) + " to [home] at your current location.");
                    openDialogueNodeEditor(player, npc, nodeName);
                    break;
                case 33:
                    npc.getDialogues().get(nodeName).set(index, "[jump]");
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<green>Changed step #" + (index + 1) + " to [jump].");
                    openDialogueNodeEditor(player, npc, nodeName);
                    break;
            }
        } else if (title.startsWith("Edit Command Type: #")) {
            event.setCancelled(true);
            String npcId = editingNpc.get(player.getUniqueId());
            NPC npc = npcManager.getNPC(npcId);
            int index = editingCommandIndex.getOrDefault(player.getUniqueId(), -1);
            if (npc == null || index == -1) return;
            if (slot == 22) { openCommandList(player, npc); return; }
            switch (slot) {
                case 11: 
                    player.closeInventory(); 
                    chatInputAction.put(player.getUniqueId(), "edit_cmd"); 
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new command for #" + (index + 1) + "."); 
                    break;
                case 13: 
                    player.closeInventory(); 
                    chatInputAction.put(player.getUniqueId(), "edit_wait"); 
                    com.eizzo.npcs.utils.ChatUtils.sendMessage(player, "<yellow>Type new wait time for #" + (index + 1) + "."); 
                    break;
                case 15: 
                    openSoundLibrary(player, npc, 0); 
                    break;
            }
        } else if (title.startsWith("Physics: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 12: npc.setCollidable(!npc.isCollidable()); npcManager.spawnNPC(npc); openPhysics(player, npc); break;
                case 14: npc.setNpcCollision(!npc.isNpcCollision()); npcManager.spawnNPC(npc); openPhysics(player, npc); break;
                case 22: openEditor(player, npc); break;
            }
            npcManager.saveNPCs();
        } else if (title.startsWith("Equipment: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            if (slot == 49) { openEditor(player, npc); return; }
            if (slot < 54) {
                EquipmentSlot es = getSlot(slot);
                if (es != null) {
                    event.setCancelled(false); // Allow clicking only in equipment slots
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        ItemStack item = event.getInventory().getItem(slot);
                        if (item != null && !item.getType().isAir() && !item.getType().name().contains("GLASS_PANE")) npc.getEquipment().put(es, item.clone());
                        else npc.getEquipment().remove(es);
                        npcManager.saveNPCs();
                        npcManager.spawnNPC(npc);
                    }, 1L);
                }
            }
        }
    }

    private EntityType cycleType(EntityType current) {
        EntityType[] types = {EntityType.VILLAGER, EntityType.PLAYER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.IRON_GOLEM, EntityType.PIG, EntityType.COW};
        for (int i = 0; i < types.length; i++) if (types[i] == current) return types[(i + 1) % types.length];
        return EntityType.VILLAGER;
    }

    private EquipmentSlot getSlot(int slot) {
        if (slot == 13) return EquipmentSlot.HEAD;
        if (slot == 21) return EquipmentSlot.HAND;
        if (slot == 22) return EquipmentSlot.CHEST;
        if (slot == 23) return EquipmentSlot.OFF_HAND;
        if (slot == 31) return EquipmentSlot.LEGS;
        if (slot == 40) return EquipmentSlot.FEET;
        return null;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!chatInputAction.containsKey(uuid)) return;
        event.setCancelled(true);
        String action = chatInputAction.remove(uuid);
        String msg = event.getMessage();
        NPC npc = npcManager.getNPC(editingNpc.get(uuid));
        if (npc == null || msg.equalsIgnoreCase("cancel")) {
            if (npc != null) Bukkit.getScheduler().runTask(plugin, () -> openEditor(event.getPlayer(), npc));
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            int index = editingCommandIndex.getOrDefault(uuid, -1);
            if (action.startsWith("add_msg:")) {
                String node = action.split(":")[1];
                npc.getDialogues().computeIfAbsent(node, k -> new ArrayList<>()).add("[msg] " + msg);
                openDialogueNodeEditor(event.getPlayer(), npc, node);
            } else if (action.startsWith("add_diag_cmd:")) {
                String node = action.split(":")[1];
                npc.getDialogues().computeIfAbsent(node, k -> new ArrayList<>()).add(msg);
                openDialogueNodeEditor(event.getPlayer(), npc, node);
            } else if (action.startsWith("add_diag_wait:")) {
                String node = action.split(":")[1];
                npc.getDialogues().computeIfAbsent(node, k -> new ArrayList<>()).add("[wait] " + msg);
                openDialogueNodeEditor(event.getPlayer(), npc, node);
            } else if (action.startsWith("add_choice:")) {
                String node = action.split(":")[1];
                if (!msg.contains("=")) {
                    event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid format! Use: Label=NodeName | Label2=NodeName"));
                } else {
                    npc.getDialogues().computeIfAbsent(node, k -> new ArrayList<>()).add("[choice] " + msg);
                }
                openDialogueNodeEditor(event.getPlayer(), npc, node);
            } else if (action.startsWith("add_diag_set:")) {
                String node = action.split(":")[1];
                npc.getDialogues().computeIfAbsent(node, k -> new ArrayList<>()).add("[set] " + msg);
                openDialogueNodeEditor(event.getPlayer(), npc, node);
            } else if (action.startsWith("add_diag_listen:")) {
                String node = action.split(":")[1];
                npc.getDialogues().computeIfAbsent(node, k -> new ArrayList<>()).add("[listen] " + msg);
                openDialogueNodeEditor(event.getPlayer(), npc, node);
            } else switch (action) {
                case "create_node":
                    npc.getDialogues().put(msg.toLowerCase(), new ArrayList<>());
                    openDialogueManager(event.getPlayer(), npc);
                    break;
                case "rename_id":
                    if (npcManager.getNPC(msg) != null) {
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<red>An NPC with that ID already exists!"));
                        openAppearance(event.getPlayer(), npc);
                    } else {
                        String oldId = npc.getId();
                        npcManager.renameNPC(oldId, msg);
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>NPC ID changed from <white>" + oldId + " <green>to <white>" + msg));
                        openAppearance(event.getPlayer(), npc);
                    }
                    break;
                case "name": npc.setName(msg); openAppearance(event.getPlayer(), npc); break;
                case "skin": npc.setSkinName(msg); npc.setSkinValue(null); npc.setSkinSignature(null); openAppearance(event.getPlayer(), npc); break;
                case "respawn_delay":
                    try {
                        int delay = Integer.parseInt(msg);
                        if (delay < 1) delay = 1;
                        if (delay > 3600) delay = 3600;
                        npc.setRespawnDelay(delay);
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Respawn delay set to <white>" + delay + "s"));
                    } catch (Exception ignored) {}
                    openCombatSettings(event.getPlayer(), npc);
                    break;
                case "max_health":
                    try {
                        double health = Double.parseDouble(msg);
                        if (health < 1) health = 1;
                        npc.setMaxHealth(health);
                        npc.setCurrentHealth(health);
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Max health set to <white>" + health));
                    } catch (Exception ignored) {}
                    openCombatSettings(event.getPlayer(), npc);
                    break;
                case "reward_vault":
                    try {
                        double amt = Double.parseDouble(msg);
                        npc.setVaultReward(Math.max(0, amt));
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Vault reward set to <white>$" + npc.getVaultReward()));
                    } catch (Exception ignored) {}
                    openRewardSettings(event.getPlayer(), npc);
                    break;
                case "reward_token_amount":
                    try {
                        double amt = Double.parseDouble(msg);
                        npc.setTokenReward(Math.max(0, amt));
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Token reward set to <white>" + npc.getTokenReward()));
                    } catch (Exception ignored) {}
                    openRewardSettings(event.getPlayer(), npc);
                    break;
                case "reward_token_id":
                    npc.setRewardTokenId(msg.toLowerCase());
                    event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Reward Token ID set to <white>" + npc.getRewardTokenId()));
                    openRewardSettings(event.getPlayer(), npc);
                    break;
                case "reward_limit":
                    try {
                        int limit = Integer.parseInt(msg);
                        npc.setRewardLimit(Math.max(0, limit));
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Max reward count set to <white>" + (npc.getRewardLimit() == 0 ? "Unlimited" : npc.getRewardLimit())));
                    } catch (Exception ignored) {}
                    openRewardSettings(event.getPlayer(), npc);
                    break;
                case "manual_tp":
                    try {
                        String[] p = msg.split(" ");
                        if (p.length < 6) {
                            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid format! Use: <white>world x y z yaw pitch"));
                        } else {
                            org.bukkit.World w = Bukkit.getWorld(p[0]);
                            if (w == null) { event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid world!")); }
                            else {
                                double x = Double.parseDouble(p[1]);
                                double y = Double.parseDouble(p[2]);
                                double z = Double.parseDouble(p[3]);
                                float yaw = Float.parseFloat(p[4]);
                                float pitch = Float.parseFloat(p[5]);
                                npc.setLocation(new Location(w, x, y, z, yaw, pitch));
                                event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>NPC location updated manually."));
                            }
                        }
                    } catch (Exception e) { event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<red>Error parsing coordinates!")); }
                    openBehavior(event.getPlayer(), npc);
                    break;
                case "cmd": npc.getCommands().add(msg); openCommandList(event.getPlayer(), npc); break;
                case "wait":
                    try {
                        double s = Double.parseDouble(msg);
                        if (s < 0.01) s = 0.01;
                        if (s > 60) s = 60;
                        npc.getCommands().add("[wait] " + s);
                    } catch (Exception ignored) {}
                    openCommandList(event.getPlayer(), npc);
                    break;
                case "edit_cmd":
                    if (index != -1 && index < npc.getCommands().size()) {
                        npc.getCommands().set(index, msg);
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Updated command #" + (index + 1)));
                    }
                    openCommandList(event.getPlayer(), npc);
                    break;
                case "edit_wait":
                    if (index != -1 && index < npc.getCommands().size()) {
                        try {
                            double s = Double.parseDouble(msg);
                            if (s < 0.01) s = 0.01;
                            if (s > 60) s = 60;
                            npc.getCommands().set(index, "[wait] " + s);
                            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Updated delay #" + (index + 1)));
                        } catch (Exception ignored) {}
                    }
                    openCommandList(event.getPlayer(), npc);
                    break;
                case "edit_diag_msg":
                    if (index != -1) {
                        String node = editingDialogueNode.get(uuid);
                        npc.getDialogues().get(node).set(index, "[msg] " + msg);
                        openDialogueNodeEditor(event.getPlayer(), npc, node);
                    }
                    break;
                case "edit_diag_cmd":
                    if (index != -1) {
                        String node = editingDialogueNode.get(uuid);
                        npc.getDialogues().get(node).set(index, msg);
                        openDialogueNodeEditor(event.getPlayer(), npc, node);
                    }
                    break;
                case "edit_diag_wait":
                    if (index != -1) {
                        String node = editingDialogueNode.get(uuid);
                        npc.getDialogues().get(node).set(index, "[wait] " + msg);
                        openDialogueNodeEditor(event.getPlayer(), npc, node);
                    }
                    break;
                case "edit_diag_choice":
                    if (index != -1) {
                        String node = editingDialogueNode.get(uuid);
                        npc.getDialogues().get(node).set(index, "[choice] " + msg);
                        openDialogueNodeEditor(event.getPlayer(), npc, node);
                    }
                    break;
                case "edit_diag_listen":
                    if (index != -1) {
                        String node = editingDialogueNode.get(uuid);
                        npc.getDialogues().get(node).set(index, "[listen] " + msg);
                        openDialogueNodeEditor(event.getPlayer(), npc, node);
                    }
                    break;
            }
            npcManager.spawnNPC(npc);
            npcManager.saveNPCs();
        });
    }

}

