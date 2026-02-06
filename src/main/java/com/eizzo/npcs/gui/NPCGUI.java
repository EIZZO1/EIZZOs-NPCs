package com.eizzo.npcs.gui;

import com.eizzo.npcs.EizzoNPCs;
import com.eizzo.npcs.managers.NPCManager;
import com.eizzo.npcs.models.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
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
        inv.setItem(15, createItem(Material.CHEST, "<yellow>Equipment", "<gray>Armor and hand items."));
        inv.setItem(16, createItem(Material.BARRIER, "<red>Delete NPC", "<gray>Remove permanently."));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back to List"));
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
        inv.setItem(15, createItem(Material.ENDER_PEARL, "<yellow>Teleport Here", "<gray>Move NPC to you."));
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
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Edit Step Type: #" + (index + 1)));
        fill(inv);
        inv.setItem(10, createItem(Material.WRITABLE_BOOK, "<green>Change to Message", "<gray>Click to type a new chat message."));
        inv.setItem(11, createItem(Material.COMMAND_BLOCK, "<green>Change to Command", "<gray>Click to type a server command."));
        inv.setItem(12, createItem(Material.CLOCK, "<aqua>Change to Wait", "<gray>Click to type wait time (0.01-60s)."));
        inv.setItem(13, createItem(Material.JUKEBOX, "<yellow>Change to Sound", "<gray>Browse the sound library."));
        inv.setItem(14, createItem(Material.HOPPER, "<gold>Change to Choice", "<gray>Click to type branching options."));
        inv.setItem(22, createItem(Material.ARROW, "<gray>Back to Node Editor"));
        player.openInventory(inv);
        editingCommandIndex.put(player.getUniqueId(), index);
        editingDialogueNode.put(player.getUniqueId(), nodeName);
    }

    public void openPropertyLibrary(Player player, NPC npc, String nodeName) {
        Inventory inv = Bukkit.createInventory(null, 36, Component.text("Property Presets: " + nodeName));
        fill(inv);

        // Define Presets
        inv.setItem(10, createPropertyItem(Material.IRON_SWORD, "Become Hostile", "hostile=true", "hostile", npc.isHostile(), true));
        inv.setItem(11, createPropertyItem(Material.SHEARS, "Become Passive", "hostile=false", "hostile", npc.isHostile(), false));
        inv.setItem(12, createPropertyItem(Material.ENDER_EYE, "Start Following", "trackingmode=FOLLOW", "trackingmode", npc.getTrackingMode(), NPC.TrackingMode.FOLLOW));
        inv.setItem(13, createPropertyItem(Material.BARRIER, "Stop Tracking", "trackingmode=NONE", "trackingmode", npc.getTrackingMode(), NPC.TrackingMode.NONE));
        inv.setItem(14, createPropertyItem(Material.FEATHER, "Start Flying", "flying=true", "flying", npc.isFlying(), true));
        inv.setItem(15, createPropertyItem(Material.ANVIL, "Enable Collision", "collidable=true", "collidable", npc.isCollidable(), true));
        inv.setItem(16, createPropertyItem(Material.SLIME_BALL, "Disable Collision", "collidable=false", "collidable", npc.isCollidable(), false));
        
        // Combos
        inv.setItem(21, createPropertyItem(Material.NETHERITE_SWORD, "Aggressive Stance", "hostile=true;trackingmode=FOLLOW", "Combo", "N/A", "Hostile + Follow"));
        inv.setItem(22, createPropertyItem(Material.BLUE_ICE, "Frozen Stance", "hostile=false;trackingmode=NONE", "Combo", "N/A", "Passive + Still"));

        inv.setItem(30, createItem(Material.WRITABLE_BOOK, "<aqua>Manual Entry", "<gray>Click to type custom property set", "<gray>e.g. skin=Steve;cape=false"));
        inv.setItem(31, createItem(Material.BARRIER, "<red>Back"));

        player.openInventory(inv);
        editingDialogueNode.put(player.getUniqueId(), nodeName);
    }

    private ItemStack createPropertyItem(Material mat, String title, String cmd, String propName, Object current, Object target) {
        return createItem(mat, "<yellow>" + title, 
            "<gray>Property: <white>" + propName,
            "<gray>Current: <white>" + current.toString(),
            "<gray>Sets to: <green>" + target.toString(),
            "",
            "<yellow>Click to add to sequence");
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
        inv.setItem(12, createItem(Material.ANVIL, "<yellow>Player Collision", "<gray>Push Players: " + (npc.isCollidable() ? "<green>YES" : "<red>NO")));
        inv.setItem(14, createItem(Material.SLIME_BALL, "<yellow>NPC Collision", "<gray>Push NPCs: " + (npc.isNpcCollision() ? "<green>YES" : "<red>NO")));
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
        
        inv.setItem(46, createItem(Material.WRITABLE_BOOK, "<green>Add Message", "<gray>Send chat to player."));
        inv.setItem(47, createItem(Material.NETHER_STAR, "<green>Add Command", "<gray>Run server command."));
        inv.setItem(48, createItem(Material.CLOCK, "<aqua>Add Wait", "<gray>Pause for X seconds."));
        inv.setItem(49, createItem(Material.JUKEBOX, "<yellow>Add Sound", "<gray>Play an effect."));
        inv.setItem(50, createItem(Material.HOPPER, "<gold>Add Choice", "<gray>Add clickable reactions."));
        inv.setItem(51, createItem(Material.REPEATER, "<light_purple>Add Property Set", "<gray>Set temp property (e.g. hostile=true)"));
        inv.setItem(53, createItem(Material.ARROW, "<gray>Back"));
        player.openInventory(inv);
        editingNpc.put(player.getUniqueId(), npc.getId());
        chatInputAction.put(player.getUniqueId(), "dialog_node:" + nodeName);
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
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Created NPC with ID: <white>setid" + i));
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
                case 15: openEquipmentEditor(player, npc); break;
                case 16: openDeleteConfirmation(player, npc); break;
                case 22: openList(player); break;
            }
        } else if (title.startsWith("Delete NPC: ")) {
            event.setCancelled(true);
            String id = title.substring(title.indexOf(": ") + 2, title.lastIndexOf("?"));
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 11:
                    npcManager.deleteNPC(npc.getId());
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>NPC deleted successfully."));
                    openList(player);
                    break;
                case 15:
                    openEditor(player, npc);
                    break;
            }
        } else if (title.startsWith("Appearance: ")) {
            event.setCancelled(true);
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            switch (slot) {
                case 10: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "rename_id"); player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type new internal ID in chat.")); break;
                case 11: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "name"); player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type new name in chat.")); break;
                case 12: npc.setType(cycleType(npc.getType())); npcManager.spawnNPC(npc); openAppearance(player, npc); break;
                case 13: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "skin"); player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type player name or file name in chat.")); break;
                case 14: npc.setShowCape(!npc.isShowCape()); npcManager.spawnNPC(npc); openAppearance(player, npc); break;
                case 15: npc.setNametagVisible(!npc.isNametagVisible()); npcManager.spawnNPC(npc); openAppearance(player, npc); break;
                case 22: openEditor(player, npc); break;
            }
            npcManager.saveNPCs();
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
                case 15: npc.setLocation(player.getLocation()); npcManager.spawnNPC(npc); openBehavior(player, npc); break;
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
            if (slot == 49) {
                player.closeInventory();
                chatInputAction.put(player.getUniqueId(), "create_node");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type new Dialogue Node name (e.g. 'start', 'yes')."));
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
            } else {
                switch (slot) {
                    case 46: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_msg:" + nodeName); player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type message to send.")); break;
                    case 47: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_diag_cmd:" + nodeName); player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type command to run.")); break;
                    case 48: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_diag_wait:" + nodeName); player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type seconds to wait (0.01 - 60).")); break;
                    case 49: openSoundLibrary(player, npc, 0); break;
                    case 50: player.closeInventory(); chatInputAction.put(player.getUniqueId(), "add_choice:" + nodeName); 
                             player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type choices format: <white>Label1=NodeName | Label2=NodeName")); break;
                    case 51: openPropertyLibrary(player, npc, nodeName); break;
                }
            }
            npcManager.saveNPCs();
        } else if (title.startsWith("Property Presets: ")) {
            event.setCancelled(true);
            String nodeName = title.split(": ")[1];
            NPC npc = npcManager.getNPC(editingNpc.get(player.getUniqueId()));
            if (npc == null) return;
            
            if (slot == 31) { openDialogueNodeEditor(player, npc, nodeName); return; }
            if (slot == 30) {
                player.closeInventory();
                chatInputAction.put(player.getUniqueId(), "add_diag_set:" + nodeName);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type property set format: <white>key1=val1;key2=val2"));
                return;
            }

            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                ItemMeta meta = item.getItemMeta();
                List<Component> lore = meta.lore();
                if (lore != null && lore.size() > 0) {
                    String propLine = PlainTextComponentSerializer.plainText().serialize(lore.get(0));
                    String valLine = PlainTextComponentSerializer.plainText().serialize(lore.get(2));
                    
                    // We can derive the command from the preset logic or store it in the item.
                    // Let's use a cleaner approach: switch on slot.
                    String setCmd = "";
                    switch (slot) {
                        case 10: setCmd = "hostile=true"; break;
                        case 11: setCmd = "hostile=false"; break;
                        case 12: setCmd = "trackingmode=FOLLOW"; break;
                        case 13: setCmd = "trackingmode=NONE"; break;
                        case 14: setCmd = "flying=true"; break;
                        case 15: setCmd = "collidable=true"; break;
                        case 16: setCmd = "collidable=false"; break;
                        case 21: setCmd = "hostile=true;trackingmode=FOLLOW"; break;
                        case 22: setCmd = "hostile=false;trackingmode=NONE"; break;
                    }
                    
                    if (!setCmd.isEmpty()) {
                        npc.getDialogues().get(nodeName).add("[set] " + setCmd);
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Added property set: <white>" + setCmd));
                        npcManager.saveNPCs();
                        openDialogueNodeEditor(player, npc, nodeName);
                    }
                }
            }
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
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type new command for #" + (index + 1) + ".")); 
                    break;
                case 13: 
                    player.closeInventory(); 
                    chatInputAction.put(player.getUniqueId(), "edit_wait"); 
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Type new wait time for #" + (index + 1) + ".")); 
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
            String id = title.split(": ")[1];
            NPC npc = npcManager.getNPC(id);
            if (npc == null) return;
            if (slot == 49) { event.setCancelled(true); openEditor(player, npc); return; }
            if (slot < 54) {
                EquipmentSlot es = getSlot(slot);
                if (es != null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        ItemStack item = event.getInventory().getItem(slot);
                        if (item != null && !item.getType().isAir() && !item.getType().name().contains("GLASS_PANE")) npc.getEquipment().put(es, item.clone());
                        else npc.getEquipment().remove(es);
                        npcManager.saveNPCs();
                        npcManager.spawnNPC(npc);
                    }, 1L);
                } else event.setCancelled(true);
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
            }
            npcManager.spawnNPC(npc);
            npcManager.saveNPCs();
        });
    }
}
