package com.eizzo.npcs.commands;
import com.eizzo.npcs.managers.NPCManager;
import com.eizzo.npcs.models.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public class NPCTabCompleter implements TabCompleter {
    private final NPCManager npcManager;
    private final List<String> subcommands = Arrays.asList(
            "create", "delete", "list", "set", "tp", "addcmd", "clearcmds", "debug", "help"
    );
    private final List<String> setProperties = Arrays.asList(
            "name", "type", "skin", "trackingmode", "trackingrange", 
            "cape", "collision", "npccollision", "flying", "returntospawn", "runmode", "nametag", "sound", "godmode", "respawndelay"
    );
    public NPCTabCompleter(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        boolean isAdmin = sender.hasPermission("eizzo.npcs.admin");
        if (args.length == 1) {
            return subcommands.stream()
                    .filter(s -> !s.equalsIgnoreCase("dialog"))
                    .filter(s -> isAdmin || s.equalsIgnoreCase("help"))
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (!isAdmin) return new ArrayList<>();
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("help")) return new ArrayList<>();
            return npcManager.getAllNPCs().stream()
                    .map(NPC::getId)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                return setProperties.stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("addcmd")) {
                return Arrays.asList("[wait] ", "[sound] ").stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set")) {
                String prop = args[2].toLowerCase();
                switch (prop) {
                    case "type":
                        return Arrays.stream(EntityType.values())
                                .map(Enum::name)
                                .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                                .limit(20)
                                .collect(Collectors.toList());
                    case "skin":
                        List<String> completions = new ArrayList<>();
                        File folder = npcManager.getSkinsFolder();
                        if (folder.exists() && folder.isDirectory()) {
                            File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
                            if (files != null) {
                                for (File f : files) completions.add(f.getName().replace(".txt", ""));
                            }
                        }
                        return completions.stream()
                                .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                                .collect(Collectors.toList());
                    case "trackingmode":
                        return Arrays.stream(NPC.TrackingMode.values())
                                .map(Enum::name)
                                .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                                .collect(Collectors.toList());
                    case "trackingrange":
                        return Arrays.asList("5", "10", "15", "20", "25", "30", "50").stream()
                                .filter(s -> s.startsWith(args[3]))
                                .collect(Collectors.toList());
                    case "cape":
                    case "collision":
                    case "npccollision":
                    case "flying":
                    case "returntospawn":
                    case "nametag":
                    case "godmode":
                        return Arrays.asList("true", "false").stream().filter(s -> s.startsWith(args[3].toLowerCase())).collect(Collectors.toList());
                    case "respawndelay":
                        return Arrays.asList("5", "10", "30", "60").stream().filter(s -> s.startsWith(args[3])).collect(Collectors.toList());
                    case "runmode":
                        return Arrays.asList("op", "console", "player").stream().filter(s -> s.startsWith(args[3].toLowerCase())).collect(Collectors.toList());
                    case "sound":
                        return Arrays.stream(org.bukkit.Sound.values())
                                .map(s -> s.key().toString())
                                .filter(s -> s.startsWith(args[3].toLowerCase()))
                                .limit(100)
                                .collect(Collectors.toList());
                }
            } else if (args[0].equalsIgnoreCase("addcmd")) {
                if (args[2].equalsIgnoreCase("[sound]")) {
                    return Arrays.stream(org.bukkit.Sound.values())
                            .map(s -> s.key().toString())
                            .filter(s -> s.startsWith(args[3].toLowerCase()))
                            .limit(100)
                            .collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }

}
