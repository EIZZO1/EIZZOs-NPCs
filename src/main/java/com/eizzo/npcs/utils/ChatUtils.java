package com.eizzo.npcs.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class ChatUtils {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final String PREFIX = "<gradient:#ffaa00:#ffff55><b>[NPCs]</b></gradient> ";
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(mm.deserialize(PREFIX + "<gray>" + message));
    }
    
    public static void sendHelpMessage(CommandSender sender, String command, String description) {
         sender.sendMessage(mm.deserialize("<dark_gray>» <gold>/npc " + command + " <dark_gray>- <gray>" + description));
    }

    public static Component format(String message) {
        return mm.deserialize(PREFIX + message);
    }
}