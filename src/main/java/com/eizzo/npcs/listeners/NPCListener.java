package com.eizzo.npcs.listeners;

import com.eizzo.npcs.EizzoNPCs;
import com.eizzo.npcs.managers.NPCManager;
import com.eizzo.npcs.models.NPC;
import com.eizzo.npcs.gui.NPCGUI;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import java.lang.reflect.Field;
import java.util.*;

public class NPCListener implements Listener {
    private final EizzoNPCs plugin;
    private final NPCManager npcManager;
    private final NPCGUI npcGui;
    private final Map<UUID, Long> interactionCooldown = new HashMap<>();
    private final Map<UUID, ListenAction> pendingListens = new HashMap<>();

    public NPCListener(EizzoNPCs plugin, NPCManager npcManager, NPCGUI npcGui) {
        this.plugin = plugin;
        this.npcManager = npcManager;
        this.npcGui = npcGui;
    }

    private static class ListenAction {
        final NPC npc;
        final List<String> queue;
        final int nextIndex;
        final boolean isDialogue;
        final String world;
        final int x, y, z;

        ListenAction(NPC npc, List<String> queue, int nextIndex, boolean isDialogue, String world, int x, int y, int z) {
            this.npc = npc;
            this.queue = queue;
            this.nextIndex = nextIndex;
            this.isDialogue = isDialogue;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getBlockX() == event.getFrom().getBlockX() && 
            event.getTo().getBlockY() == event.getFrom().getBlockY() && 
            event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;

        UUID uuid = event.getPlayer().getUniqueId();
        ListenAction action = pendingListens.get(uuid);
        if (action == null) return;

        if (event.getTo().getWorld().getName().equals(action.world) &&
            event.getTo().getBlockX() == action.x &&
            event.getTo().getBlockY() == action.y &&
            event.getTo().getBlockZ() == action.z) {
            
            pendingListens.remove(uuid);
            processActionQueue(event.getPlayer(), action.npc, action.queue, action.nextIndex, action.isDialogue);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        injectPlayer(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (NPC npc : npcManager.getAllNPCs()) {
                npcManager.showToPlayer(player, npc);
            }
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
        npcManager.cleanupPlayer(event.getPlayer());
    }

    @EventHandler
    public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent event) {
        for (org.bukkit.entity.Entity entity : event.getChunk().getEntities()) {
            if (entity.getScoreboardTags().contains("EIZZO_NPC")) {
                entity.remove();
            }
        }
    }

    private void handleNPCAttack(Player player, NPC npc) {
        npcManager.broadcastAnimation(npc, 0); // Swing
        executeCommands(player, npc, true);
    }

    private void handleNPCInteract(Player player, NPC npc) {
        if (player.isSneaking() && player.hasPermission("eizzo.npcs.admin")) {
            npcGui.openEditor(player, npc);
            return;
        }
        if (npc.getDialogues().containsKey("start")) {
            executeDialogue(player, npc, "start");
        } else {
            executeCommands(player, npc, true);
        }
    }

            public void executeDialogue(Player player, NPC npc, String nodeName) {

                List<String> sequence = npc.getDialogues().get(nodeName);

                if (sequence != null) {

                    plugin.getLogger().info("Executing dialogue node '" + nodeName + "' for player " + player.getName());

                    processActionQueue(player, npc, sequence, 0, true);

                } else {

                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<red>Error: Dialogue node '<white>" + nodeName + "<red>' not found for this NPC!"));

                }

            }

        

    

        private void executeCommands(Player player, NPC npc, boolean playInteractSound) {

            if (playInteractSound && npc.getInteractSound() != null && !npc.getInteractSound().isEmpty()) {

                player.playSound(player.getLocation(), npc.getInteractSound(), 1.0f, 1.0f);

            }

            List<String> commands = new ArrayList<>(npc.getCommands());

            processActionQueue(player, npc, commands, 0, false);

        }

    

        private void processActionQueue(Player player, NPC npc, List<String> queue, int index, boolean isDialogue) {

            if (!player.isOnline()) return;

            

                        if (index >= queue.size()) {

            

                            if (isDialogue) {

            

                                npcManager.resetPlayerOverrides(player, npc);

            

                                executeCommands(player, npc, false); // Execute standard commands after dialogue finishes

            

                            }

            

                            return;

            

                        }

            

                

            

                        String action = queue.get(index);

            

                        if (action == null || action.trim().isEmpty()) {

            

                            processActionQueue(player, npc, queue, index + 1, isDialogue);

            

                            return;

            

                        }

            

                

            

                                    if (action.startsWith("[set]")) {

            

                

            

                                        try {

            

                

            

                                            String data = action.substring(action.startsWith("[set] ") ? 6 : 5);

            

                

            

                                            String[] pairs = data.split(";");

            

                

            

                                            for (String pair : pairs) {

            

                

            

                                                String[] parts = pair.split("=", 2);

            

                

            

                                                if (parts.length == 2) {

            

                

            

                                                    String key = parts[0].trim().toLowerCase();

            

                

            

                                                    String val = parts[1].trim();

            

                

            

                                                    

            

                

            

                                                    Object finalVal = val;

            

                

            

                                                    if (val.equalsIgnoreCase("true")) finalVal = true;

            

                

            

                                                    else if (val.equalsIgnoreCase("false")) finalVal = false;

            

                

            

                                                    else if (key.equals("trackingmode")) {

            

                

            

                                                        try { finalVal = NPC.TrackingMode.valueOf(val.toUpperCase()); } catch (Exception ignored) {}

            

                

            

                                                    }

            

                

            

                            

            

                

            

                                                    npcManager.setPlayerOverride(player, npc, key, finalVal);

            

                

            

                                                }

            

                

            

                                            }

            

                

            

                                        } catch (Exception e) { e.printStackTrace(); }

            

                

            

                                        processActionQueue(player, npc, queue, index + 1, isDialogue);

            

                

            

                                        return;

            

                

            

                                    } else if (action.startsWith("[listen]")) {
                try {
                    String data = action.substring(action.startsWith("[listen] ") ? 9 : 8).trim();
                    String[] parts = data.split(" ");
                    String world = player.getWorld().getName();
                    int x, y, z;
                    if (parts.length >= 4) {
                        world = parts[0];
                        x = Integer.parseInt(parts[1]);
                        y = Integer.parseInt(parts[2]);
                        z = Integer.parseInt(parts[3]);
                    } else if (parts.length == 3) {
                        x = Integer.parseInt(parts[0]);
                        y = Integer.parseInt(parts[1]);
                        z = Integer.parseInt(parts[2]);
                    } else return;

                    pendingListens.put(player.getUniqueId(), new ListenAction(npc, queue, index + 1, isDialogue, world, x, y, z));
                    return; 
                } catch (Exception e) { 
                    e.printStackTrace();
                    processActionQueue(player, npc, queue, index + 1, isDialogue);
                }
                return;
            } else if (action.startsWith("[home]")) {
                npc.setLocation(player.getLocation().clone());
                npcManager.saveNPC(npc);
                processActionQueue(player, npc, queue, index + 1, isDialogue);
                return;
            } else if (action.startsWith("[wait]")) {

                try {

                    String val = action.substring(action.startsWith("[wait] ") ? 7 : 6);

                    double seconds = Double.parseDouble(val);

                    long ticks = (long) (seconds * 20L);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> processActionQueue(player, npc, queue, index + 1, isDialogue), ticks);

                    return;

                } catch (Exception ignored) {}

            } else if (action.startsWith("[sound]")) {

                String soundName = action.substring(action.startsWith("[sound] ") ? 8 : 7);

                player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);

                processActionQueue(player, npc, queue, index + 1, isDialogue);

                return;

            } else if (action.startsWith("[msg]")) {

                String msg = action.substring(action.startsWith("[msg] ") ? 6 : 5).replace("%player%", player.getName());

                String npcName = npc.getName();

                // Format as a nice "local chat" message

                String formatted = "<yellow>" + npcName + " <gray>» <white>" + msg;

                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(formatted));

                processActionQueue(player, npc, queue, index + 1, isDialogue);

                return;

                    } else if (action.startsWith("[choice]")) {

                        String choicesData = action.substring(action.startsWith("[choice] ") ? 9 : 8);

                        String[] choices = choicesData.split("\\s*\\|\\s*");

                        net.kyori.adventure.text.TextComponent.Builder builder = net.kyori.adventure.text.Component.text();

                        

                        for (int i = 0; i < choices.length; i++) {

                            String[] parts = choices[i].split("\\s*=\\s*", 2);

                            if (parts.length == 2) {

                                String label = parts[0].trim();

                                String nextNode = parts[1].trim();

                                builder.append(net.kyori.adventure.text.Component.text("[" + label + "]")

            

                                .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)

                                .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)

                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/npc dialog " + npc.getId() + " " + nextNode))

                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text("Click to choose " + label))));

                        if (i < choices.length - 1) builder.append(net.kyori.adventure.text.Component.text("  "));

                    }

                }

                player.sendMessage(builder.build());

                plugin.getLogger().info("Sent dialogue choices to " + player.getName());

                return; // Choices stop the automatic queue until player clicks

            } else {

                String finalCmd = action.replace("%player%", player.getName());

                if (npc.isRunAsConsole()) {

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);

                } else if (npc.isRunAsOp()) {

                    boolean op = player.isOp();

                    try {

                        player.setOp(true);

                        player.performCommand(finalCmd);

                    } finally {

                        player.setOp(op);

                    }

                } else {

                    player.performCommand(finalCmd);

                }

    

                processActionQueue(player, npc, queue, index + 1, isDialogue);

            }

        }

    

    private void injectPlayer(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = handle.getClass().getField("connection").get(handle);
            Object network = connection.getClass().getField("connection").get(connection);
            io.netty.channel.Channel channel = (io.netty.channel.Channel) network.getClass().getField("channel").get(network);

            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                    if (packet.getClass().getSimpleName().equals("ServerboundInteractPacket")) {
                        handleInteractPacket(player, packet);
                    }
                    super.channelRead(ctx, packet);
                }
            };

            ChannelPipeline pipeline = channel.pipeline();
            if (pipeline.get(player.getName()) != null) pipeline.remove(player.getName());
            pipeline.addBefore("packet_handler", player.getName(), handler);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleInteractPacket(Player player, Object packet) {
        try {
            int entityId = -1;
            boolean isAttack = false;
            
            for (Field f : packet.getClass().getDeclaredFields()) {
                if (f.getType() == int.class) {
                    f.setAccessible(true);
                    entityId = f.getInt(packet);
                    break;
                }
            }
            
            for (Field f : packet.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object action = f.get(packet);
                if (action == null || action instanceof Integer || action instanceof Boolean) continue;
                
                String className = action.getClass().getSimpleName();
                if (className.contains("Attack")) {
                    isAttack = true;
                    break;
                } else if (className.contains("Interact")) {
                    isAttack = false;
                    break;
                }
                
                String actionStr = action.toString().toUpperCase();
                if (actionStr.contains("ATTACK")) {
                    isAttack = true;
                    break;
                } else if (actionStr.contains("INTERACT")) {
                    isAttack = false;
                    break;
                }

                for (Field innerF : action.getClass().getDeclaredFields()) {
                    if (innerF.getType().isEnum()) {
                        innerF.setAccessible(true);
                        Object enumVal = innerF.get(action);
                        if (enumVal != null) {
                            String enumStr = enumVal.toString().toUpperCase();
                            if (enumStr.contains("ATTACK")) {
                                isAttack = true;
                                break;
                            } else if (enumStr.contains("INTERACT")) {
                                isAttack = false;
                                break;
                            }
                        }
                    }
                }
                if (isAttack) break;
            }

            if (entityId != -1) {
                NPC npc = npcManager.getNPCByEntityId(entityId);
                if (npc != null) {
                    long now = System.currentTimeMillis();
                    if (now - interactionCooldown.getOrDefault(player.getUniqueId(), 0L) < 200) return;
                    interactionCooldown.put(player.getUniqueId(), now);

                    final boolean finalAttack = isAttack;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (finalAttack) handleNPCAttack(player, npc);
                        else handleNPCInteract(player, npc);
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void removePlayer(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = handle.getClass().getField("connection").get(handle);
            Object network = connection.getClass().getField("connection").get(connection);
            io.netty.channel.Channel channel = (io.netty.channel.Channel) network.getClass().getField("channel").get(network);
            channel.eventLoop().submit(() -> {
                if (channel.pipeline().get(player.getName()) != null) channel.pipeline().remove(player.getName());
                return null;
            });
        } catch (Exception ignored) {}
    }
}