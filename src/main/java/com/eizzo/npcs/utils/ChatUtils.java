package com.eizzo.npcs.utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
public class ChatUtils {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final String PREFIX = "<gradient:#00FFFF:#55FFFF><b>[EIZZOs-NPCs]</b></gradient> <gray>» </gray>";
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(mm.deserialize(PREFIX + "<white>" + message));
    }

    public static void sendHelpHeader(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<gradient:#00FFFF:#55FFFF><b>--- EIZZOs-NPCs ---</b></gradient>"));
    }

    public static void sendHelpMessage(CommandSender sender, String command, String description) {
         sender.sendMessage(mm.deserialize("<gradient:#00FFFF:#55FFFF><b>»</b></gradient> <aqua>/npc " + command + "</aqua> <gray>-- " + description));
    }

    public static void sendNPCMessage(CommandSender sender, String npcName, String message) {
        sender.sendMessage(mm.deserialize("<gradient:#00FFFF:#55FFFF><b>[" + npcName + "]:</b></gradient> <white>" + message));
    }

    public static Component format(String message) {
        return mm.deserialize(message);
    }

}

