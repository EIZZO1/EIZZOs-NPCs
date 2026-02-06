package com.eizzo.npcs.commands;

import com.eizzo.npcs.EizzoNPCs;
import com.eizzo.npcs.managers.NPCManager;
import com.eizzo.npcs.models.NPC;
import com.eizzo.npcs.gui.NPCGUI;
import com.eizzo.npcs.utils.ChatUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NPCCommand implements CommandExecutor {
    private final EizzoNPCs plugin;
    private final NPCManager npcManager;
    private final NPCGUI npcGui;

    public NPCCommand(EizzoNPCs plugin, NPCManager npcManager, NPCGUI npcGui) {
        this.plugin = plugin;
        this.npcManager = npcManager;
        this.npcGui = npcGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length >= 3 && args[0].equalsIgnoreCase("dialog")) {
                String id = args[1];
                String node = args[2];
                String targetName = args.length > 3 ? args[3] : null;
                if (targetName == null) {
                    sender.sendMessage("Usage: /npc dialog <id> <node> <player>");
                    return true;
                }
                Player target = org.bukkit.Bukkit.getPlayer(targetName);
                if (target == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                NPC npc = npcManager.getNPC(id);
                if (npc == null) {
                    sender.sendMessage("NPC not found.");
                    return true;
                }
                plugin.getNpcListener().executeDialogue(target, npc, node);
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
                sendHelp(sender);
            } else {
                sender.sendMessage("Console must use arguments. Type /npc help for info.");
            }
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            if (!player.hasPermission("eizzo.npcs.admin")) {
                ChatUtils.sendMessage(player, "<red>You don't have permission to perform this command.");
                return true;
            }
            npcGui.openList(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        
        if (sub.equals("help")) {
            sendHelp(player);
            return true;
        }

        if (sub.equals("dialog")) {
            if (args.length < 3) return true;
            String nodeName = args[2];
            String token = args.length > 3 ? args[3] : null;

            boolean authorized = false;
            if (player.hasPermission("eizzo.npcs.admin")) {
                authorized = true;
            } else if (token != null) {
                authorized = plugin.getNpcListener().validateToken(player, token, nodeName);
            }

            if (!authorized) {
                ChatUtils.sendMessage(player, "<red>You don't have permission to perform this command.");
                return true;
            }

            NPC npc = npcManager.getNPC(args[1]);
            if (npc == null) {
                ChatUtils.sendMessage(player, "<red>NPC not found!");
                return true;
            }
            
            Location currentLoc = npcManager.getCurrentLocation(player, npc);
            // Distance check: 10 blocks (100 distance squared)
            if (player.getWorld() != currentLoc.getWorld() || player.getLocation().distanceSquared(currentLoc) > 100) {
                ChatUtils.sendMessage(player, "<red>You are too far away from this NPC.");
                return true;
            }
            
            plugin.getNpcListener().executeDialogue(player, npc, nodeName);
            return true;
        }

        if (!player.hasPermission("eizzo.npcs.admin")) {
            ChatUtils.sendMessage(player, "<red>You don't have permission to perform this command.");
            return true;
        }

        if (sub.equals("create")) {
            if (args.length < 3) { ChatUtils.sendMessage(player, "<red>Usage: /npc create <id> <name>"); return true; }
            String id = args[1];
            if (npcManager.getNPC(id) != null) { ChatUtils.sendMessage(player, "<red>An NPC with that ID already exists!"); return true; }
            StringBuilder name = new StringBuilder();
            for (int i = 2; i < args.length; i++) name.append(args[i]).append(i == args.length - 1 ? "" : " ");
            npcManager.createNPC(id, name.toString(), EntityType.VILLAGER, player.getLocation());
            ChatUtils.sendMessage(player, "NPC created!");
            return true;
        }

        if (sub.equals("list")) {
            npcGui.openList(player);
            return true;
        }

        if (args.length < 2) {
            ChatUtils.sendMessage(player, "Unknown subcommand. Type <yellow>/npc help</yellow> for help.");
            return true;
        }

        NPC npc = npcManager.getNPC(args[1]);
        if (npc == null) {
            ChatUtils.sendMessage(player, "<red>NPC not found!");
            return true;
        }

        switch (sub) {
            case "delete":
                npcManager.deleteNPC(npc.getId());
                ChatUtils.sendMessage(player, "NPC deleted.");
                break;
            case "set":
                handleSet(player, npc, args);
                break;
            case "tp":
                npc.setLocation(player.getLocation());
                npcManager.spawnNPC(npc);
                ChatUtils.sendMessage(player, "NPC teleported to you.");
                break;
            case "addcmd":
                if (args.length < 3) { ChatUtils.sendMessage(player, "<red>Usage: /npc addcmd <id> <cmd>"); break; }
                StringBuilder cmdStr = new StringBuilder();
                for (int i = 2; i < args.length; i++) cmdStr.append(args[i]).append(i == args.length - 1 ? "" : " ");
                npc.getCommands().add(cmdStr.toString());
                ChatUtils.sendMessage(player, "Command added.");
                break;
            case "clearcmds":
                npc.getCommands().clear();
                ChatUtils.sendMessage(player, "Commands cleared.");
                break;
            default:
                ChatUtils.sendMessage(player, "<red>Unknown subcommand.");
                break;
        }

        npcManager.saveNPCs();
        return true;
    }

    private void handleSet(Player player, NPC npc, String[] args) {
        if (args.length < 3) {
            ChatUtils.sendMessage(player, "<red>Usage: /npc set <id> <property> <value>");
            return;
        }

        String prop = args[2].toLowerCase();
        if (args.length < 4) {
            ChatUtils.sendMessage(player, "<red>Specify a value for " + prop);
            return;
        }

        String val = args[3];
        switch (prop) {
            case "name":
                StringBuilder newName = new StringBuilder();
                for (int i = 3; i < args.length; i++) newName.append(args[i]).append(i == args.length - 1 ? "" : " ");
                npc.setName(newName.toString());
                npcManager.spawnNPC(npc);
                ChatUtils.sendMessage(player, "Name updated.");
                break;
            case "type":
                try {
                    npc.setType(EntityType.valueOf(val.toUpperCase()));
                    npcManager.spawnNPC(npc);
                    ChatUtils.sendMessage(player, "Type updated.");
                } catch (Exception e) { ChatUtils.sendMessage(player, "<red>Invalid type!"); }
                break;
            case "skin":
                npc.setSkinName(val);
                npc.setSkinValue(null);
                npc.setSkinSignature(null);
                npcManager.spawnNPC(npc);
                ChatUtils.sendMessage(player, "Skin updated.");
                break;
            case "trackingmode":
                try {
                    npc.setTrackingMode(NPC.TrackingMode.valueOf(val.toUpperCase()));
                    ChatUtils.sendMessage(player, "Tracking mode set.");
                } catch (Exception e) { ChatUtils.sendMessage(player, "<red>Invalid mode!"); }
                break;
            case "trackingrange":
                try {
                    npc.setTrackingRange(Double.parseDouble(val));
                    ChatUtils.sendMessage(player, "Tracking range updated.");
                } catch (Exception e) { ChatUtils.sendMessage(player, "<red>Invalid number!"); }
                break;
            case "cape":
                npc.setShowCape(Boolean.parseBoolean(val));
                npcManager.spawnNPC(npc);
                ChatUtils.sendMessage(player, "Cape visibility updated.");
                break;
            case "collision":
                npc.setCollidable(Boolean.parseBoolean(val));
                npcManager.spawnNPC(npc);
                ChatUtils.sendMessage(player, "Player collision updated.");
                break;
            case "npccollision":
                npc.setNpcCollision(Boolean.parseBoolean(val));
                npcManager.spawnNPC(npc);
                ChatUtils.sendMessage(player, "NPC collision updated.");
                break;
            case "flying":
                npc.setFlying(Boolean.parseBoolean(val));
                npcManager.spawnNPC(npc);
                ChatUtils.sendMessage(player, "Flying updated.");
                break;
            case "returntospawn":
                npc.setReturnToSpawn(Boolean.parseBoolean(val));
                ChatUtils.sendMessage(player, "Return to spawn updated.");
                break;
            case "nametag":
                npc.setNametagVisible(Boolean.parseBoolean(val));
                npcManager.spawnNPC(npc);
                ChatUtils.sendMessage(player, "Nametag visibility updated.");
                break;
            case "sound":
                npc.setInteractSound(val.equalsIgnoreCase("none") ? null : val.toLowerCase());
                ChatUtils.sendMessage(player, "Interaction sound updated.");
                break;
            case "runmode":
                if (val.equalsIgnoreCase("op")) { npc.setRunAsOp(true); npc.setRunAsConsole(false); }
                else if (val.equalsIgnoreCase("console")) { npc.setRunAsOp(false); npc.setRunAsConsole(true); }
                else { npc.setRunAsOp(false); npc.setRunAsConsole(false); }
                ChatUtils.sendMessage(player, "Run mode updated.");
                break;
            default:
                ChatUtils.sendMessage(player, "<red>Unknown property: " + prop);
                break;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#ffaa00:#ffff55><b>--- EIZZO NPCs Help ---</b></gradient>"));
        ChatUtils.sendHelpMessage(sender, "", "Open NPC list.");
        ChatUtils.sendHelpMessage(sender, "create <id> <name>", "Create a new NPC.");
        ChatUtils.sendHelpMessage(sender, "delete <id>", "Delete an NPC.");
        ChatUtils.sendHelpMessage(sender, "set <id> <property> <val>", "Set NPC properties.");
        ChatUtils.sendHelpMessage(sender, "tp <id>", "Teleport NPC to you.");
        ChatUtils.sendHelpMessage(sender, "addcmd <id> <cmd>", "Add interaction command.");
        ChatUtils.sendHelpMessage(sender, "clearcmds <id>", "Clear all commands.");
        ChatUtils.sendHelpMessage(sender, "help", "Show this menu.");
    }
}