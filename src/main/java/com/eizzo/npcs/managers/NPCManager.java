package com.eizzo.npcs.managers;

import com.eizzo.npcs.EizzoNPCs;
import com.eizzo.npcs.models.NPC;
import com.eizzo.npcs.utils.ReflectionUtils;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NPCManager {
    private final EizzoNPCs plugin;
    private final Map<String, NPC> npcs = new HashMap<>();
    private final Map<String, Integer> npcEntityIds = new HashMap<>();
    private final Map<String, UUID> npcUUIDs = new HashMap<>();
    private final Map<String, Set<UUID>> npcViewers = new HashMap<>();
    private final Map<UUID, Map<String, Location>> activeNPCLocations = new HashMap<>(); 
    private final Map<String, Long> lastAttackTime = new HashMap<>();
    private final Map<UUID, Map<String, Map<String, Object>>> playerOverrides = new HashMap<>();
    
    private final File npcFile;
    private final File skinsFolder;
    private FileConfiguration npcConfig;
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(2000000);
    private final DatabaseManager databaseManager;

    public NPCManager(EizzoNPCs plugin) {
        this.plugin = plugin;
        this.npcFile = new File(plugin.getDataFolder(), "npcs.yml");
        this.skinsFolder = new File(plugin.getDataFolder(), "skins");
        if (!skinsFolder.exists()) skinsFolder.mkdirs();
        
        this.databaseManager = new DatabaseManager(plugin);
        this.databaseManager.connect();
        
        loadNPCs();
        startTasks();
    }

    private void startTasks() {
        // Range Check Task
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (NPC npc : npcs.values()) {
                        boolean inWorld = npc.getLocation().getWorld().equals(player.getWorld());
                        boolean inRange = inWorld && npc.getLocation().distanceSquared(player.getLocation()) < 64 * 64;
                        boolean isViewer = npcViewers.getOrDefault(npc.getId(), Collections.emptySet()).contains(player.getUniqueId());
                        
                        if (inRange && !isViewer) showToPlayer(player, npc);
                        else if (!inRange && isViewer) hideFromPlayer(player, npc);
                    }
                }
            } catch (Throwable t) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error in Range Check Task", t);
            }
        }, 20L, 20L);

        // Tracking & Logic Task
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (NPC npc : npcs.values()) {
                        if (!npcViewers.getOrDefault(npc.getId(), Collections.emptySet()).contains(player.getUniqueId())) continue;
                        handleNPCTick(player, npc);
                    }
                }
            } catch (Throwable t) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error in Tracking Task", t);
            }
        }, 2L, 2L);
    }

    public void setPlayerOverride(Player player, NPC npc, String property, Object value) {
        playerOverrides.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .computeIfAbsent(npc.getId(), k -> new HashMap<>())
                .put(property, value);
        
        // If it's a visual property, respawn for player
        if (property.equals("type") || property.equals("skin") || property.equals("nametag") || property.equals("cape")) {
            hideFromPlayer(player, npc);
            showToPlayer(player, npc);
        }
    }

    public boolean hasOverride(Player player, NPC npc, String property) {
        Map<String, Map<String, Object>> playerMap = playerOverrides.get(player.getUniqueId());
        return playerMap != null && playerMap.containsKey(npc.getId()) && playerMap.get(npc.getId()).containsKey(property);
    }

    public void resetPlayerOverrides(Player player, NPC npc) {
        Map<String, Map<String, Object>> playerMap = playerOverrides.get(player.getUniqueId());
        if (playerMap != null) playerMap.remove(npc.getId());
    }

    public void restoreNPCForPlayer(Player player, NPC npc) {
        Map<String, Map<String, Object>> playerMap = playerOverrides.get(player.getUniqueId());
        if (playerMap != null && playerMap.containsKey(npc.getId())) {
            Map<String, Object> overrides = playerMap.get(npc.getId());
            if (overrides.containsKey("originalLocation")) {
                Location original = (Location) overrides.get("originalLocation");
                npc.setLocation(original);
                databaseManager.saveNPC(npc); // Revert permanent change
            }
        }
        resetPlayerOverrides(player, npc);
        hideFromPlayer(player, npc);
        showToPlayer(player, npc);
    }

    private boolean isHostile(Player player, NPC npc) {
        Map<String, Map<String, Object>> playerMap = playerOverrides.get(player.getUniqueId());
        if (playerMap != null && playerMap.containsKey(npc.getId())) {
            Map<String, Object> overrides = playerMap.get(npc.getId());
            if (overrides.containsKey("hostile")) return (boolean) overrides.get("hostile");
        }
        return npc.isHostile();
    }

    private NPC.TrackingMode getTrackingMode(Player player, NPC npc) {
        Map<String, Map<String, Object>> playerMap = playerOverrides.get(player.getUniqueId());
        if (playerMap != null && playerMap.containsKey(npc.getId())) {
            Map<String, Object> overrides = playerMap.get(npc.getId());
            if (overrides.containsKey("trackingmode")) {
                Object val = overrides.get("trackingmode");
                if (val instanceof NPC.TrackingMode) return (NPC.TrackingMode) val;
                try { return NPC.TrackingMode.valueOf(val.toString().toUpperCase()); } catch (Exception ignored) {}
            }
        }
        return npc.getTrackingMode();
    }

    private EntityType getEntityType(Player player, NPC npc) {
        Map<String, Map<String, Object>> playerMap = playerOverrides.get(player.getUniqueId());
        if (playerMap != null && playerMap.containsKey(npc.getId())) {
            Map<String, Object> overrides = playerMap.get(npc.getId());
            if (overrides.containsKey("type")) {
                Object val = overrides.get("type");
                if (val instanceof EntityType) return (EntityType) val;
                try { return EntityType.valueOf(val.toString().toUpperCase()); } catch (Exception ignored) {}
            }
        }
        return npc.getType();
    }

    private boolean isNametagVisible(Player player, NPC npc) {
        Map<String, Map<String, Object>> playerMap = playerOverrides.get(player.getUniqueId());
        if (playerMap != null && playerMap.containsKey(npc.getId())) {
            Map<String, Object> overrides = playerMap.get(npc.getId());
            if (overrides.containsKey("nametag")) return (boolean) overrides.get("nametag");
        }
        return npc.isNametagVisible();
    }

    private void handleNPCTick(Player player, NPC npc) {
        boolean sameWorld = npc.getLocation().getWorld().equals(player.getWorld());
        Location spawnLoc = npc.getLocation();
        Location currentLoc = activeNPCLocations.getOrDefault(player.getUniqueId(), Collections.emptyMap())
                .getOrDefault(npc.getId(), spawnLoc);
        
        double distSq = sameWorld ? currentLoc.distanceSquared(player.getLocation()) : Double.MAX_VALUE;
        double followRangeSq = npc.getTrackingRange() * npc.getTrackingRange();
        double resetRangeSq = 30.0 * 30.0;

        NPC.TrackingMode mode = getTrackingMode(player, npc);

        if (!sameWorld || distSq > resetRangeSq || mode == NPC.TrackingMode.NONE) {
            returnToSpawn(player, npc);
            return;
        }

        double playerDistFromSpawnSq = player.getLocation().distanceSquared(spawnLoc);
        if (playerDistFromSpawnSq > followRangeSq) {
            if (npc.isReturnToSpawn()) returnToSpawn(player, npc);
            return;
        }

        if (mode == NPC.TrackingMode.FOLLOW) {
            updateNPCMovement(player, npc, currentLoc);
        } else if (mode == NPC.TrackingMode.STILL) {
            updateNPCRotation(player, npc, currentLoc);
        }
    }

    private void returnToSpawn(Player player, NPC npc) {
        Map<String, Location> playerLocs = activeNPCLocations.get(player.getUniqueId());
        if (playerLocs != null && playerLocs.containsKey(npc.getId())) {
            teleportNPC(player, npc, npc.getLocation());
            sendRotateHeadPacket(player, npc, npc.getLocation().getYaw());
            playerLocs.remove(npc.getId());
        }
    }

    private void updateNPCMovement(Player player, NPC npc, Location currentLoc) {
        Location targetLoc = player.getLocation();
        Location spawnLoc = npc.getLocation();
        double dx = targetLoc.getX() - currentLoc.getX();
        double dy = targetLoc.getY() - currentLoc.getY();
        double dz = targetLoc.getZ() - currentLoc.getZ();
        double distSq = dx*dx + dy*dy + dz*dz;
        
        boolean hostile = isHostile(player, npc);
        double stopDist = hostile ? 1.5 : 2.5;
        if (distSq < stopDist * stopDist) {
            updateNPCRotation(player, npc, currentLoc);
            if (hostile) handleHostileAttack(player, npc);
            return;
        }

        double speed = hostile ? 0.6 : 0.4;
        double dist = Math.sqrt(distSq);
        Location nextLoc = currentLoc.clone().add((dx/dist)*speed, 0, (dz/dist)*speed);
        
        // Handle jumping and obstacle detection
        if (nextLoc.getBlock().getType().isSolid()) {
            Location jumpLoc = nextLoc.clone().add(0, 1, 0);
            if (!jumpLoc.getBlock().getType().isSolid() && !jumpLoc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                // Can jump over
                nextLoc.setY(nextLoc.getY() + 1.1); // Jump up
                broadcastAnimation(npc, 0); // Visual swing for effort
            } else {
                // Wall too high
                updateNPCRotation(player, npc, currentLoc);
                return;
            }
        }

        // Prevent phasing through walls (already implemented but refined with jump)
        if (!canFit(nextLoc, npc.getType())) {
            updateNPCRotation(player, npc, currentLoc);
            return;
        }

        if (Math.abs(nextLoc.getX() - spawnLoc.getX()) > npc.getTrackingRange() ||
            Math.abs(nextLoc.getZ() - spawnLoc.getZ()) > npc.getTrackingRange()) {
            updateNPCRotation(player, npc, currentLoc);
            return;
        }

        if (!npc.isFlying()) {
            double groundY = findGroundY(nextLoc);
            if (groundY != -999) nextLoc.setY(groundY);
            else nextLoc.setY(currentLoc.getY() - 0.5);
        } else {
            nextLoc.add(0, (dy/dist)*speed, 0);
        }
        
        if (Math.abs(nextLoc.getY() - spawnLoc.getY()) > npc.getTrackingRange()) {
            updateNPCRotation(player, npc, currentLoc);
            return;
        }

        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(targetLoc.getY() - (nextLoc.getY() + getEyeHeight(npc.getType())), dist) * 180.0D / Math.PI);
        
        teleportNPC(player, npc, nextLoc, yaw, pitch);
        sendRotateHeadPacket(player, npc, yaw);
        activeNPCLocations.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(npc.getId(), nextLoc);
    }

    private void handleHostileAttack(Player player, NPC npc) {
        long now = System.currentTimeMillis();
        String key = npc.getId() + ":" + player.getUniqueId();
        if (now - lastAttackTime.getOrDefault(key, 0L) < 1000) return; // 1s cooldown

        lastAttackTime.put(key, now);
        broadcastAnimation(npc, 0); // Swing

        double damage = 1.0;
        ItemStack hand = npc.getEquipment().get(EquipmentSlot.HAND);
        if (hand != null) {
            String type = hand.getType().name();
            if (type.contains("SWORD")) damage = 6.0;
            else if (type.contains("AXE")) damage = 7.0;
            else if (type.contains("PICKAXE")) damage = 4.0;
            else if (type.contains("SHOVEL")) damage = 3.0;
            else if (type.contains("HOE")) damage = 2.0;
        }

        final double finalDamage = damage;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && player.getWorld().equals(npc.getLocation().getWorld())) {
                player.damage(finalDamage);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
            }
        });
    }

    private void updateNPCRotation(Player player, NPC npc, Location currentLoc) {
        Location targetLoc = player.getLocation();
        double dx = targetLoc.getX() - currentLoc.getX();
        double npcEyeY = currentLoc.getY() + getEyeHeight(npc.getType());
        if (npc.getType() == EntityType.PLAYER) npcEyeY -= 0.1;

        double dy = (targetLoc.getY() + player.getEyeHeight()) - npcEyeY;
        double dz = targetLoc.getZ() - currentLoc.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, dist) * 180.0D / Math.PI);
        
        teleportNPC(player, npc, currentLoc, yaw, pitch);
        sendRotateHeadPacket(player, npc, yaw);
    }

    private void sendRotateHeadPacket(Player player, NPC npc, float yaw) {
        Integer entityId = getEntityId(npc);
        if (entityId == null) return;
        try {
            Object rotPacket = ReflectionUtils.instantiate(ReflectionUtils.CLIENTBOUND_ROTATE_HEAD);
            ReflectionUtils.setFieldByType(rotPacket, int.class, entityId);
            ReflectionUtils.setFieldByType(rotPacket, byte.class, (byte) ((yaw * 256.0F) / 360.0F));
            ReflectionUtils.sendPacket(player, rotPacket);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private double getEyeHeight(EntityType type) {
        switch (type) {
            case PLAYER: case VILLAGER: case ZOMBIE: case SKELETON: return 1.62;
            case CREEPER: return 1.58;
            case IRON_GOLEM: return 2.68;
            case PIG: return 0.76;
            case COW: return 1.3;
            default: return 1.62;
        }
    }

    private Object getNMSType(EntityType type) throws Exception {
        String name = type.name();
        Field f = ReflectionUtils.getNMSClass("net.minecraft.world.entity.EntityType").getField(name);
        return f.get(null);
    }

    public void spawnNPC(NPC npc) {
        despawnNPC(npc);
        int entityId = ID_COUNTER.incrementAndGet();
        UUID uuid = UUID.nameUUIDFromBytes(("NPC:" + npc.getId()).getBytes());
        npcEntityIds.put(npc.getId(), entityId);
        npcUUIDs.put(npc.getId(), uuid);
        npc.setEntityUuid(uuid);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(npc.getLocation().getWorld()) && p.getLocation().distanceSquared(npc.getLocation()) < 64*64) {
                showToPlayer(p, npc);
            }
        }
    }

    public void showToPlayer(Player player, NPC npc) {
        Integer entityId = npcEntityIds.get(npc.getId());
        UUID uuid = npcUUIDs.get(npc.getId());
        if (entityId == null || uuid == null) return;
        if (npcViewers.computeIfAbsent(npc.getId(), k -> new HashSet<>()).add(player.getUniqueId())) {
            try {
                EntityType type = getEntityType(player, npc);
                if (type == EntityType.PLAYER) {
                    PlayerProfile profile = Bukkit.createProfile(uuid, npc.getName().length() > 16 ? npc.getName().substring(0,16) : npc.getName());
                    if (npc.getSkinValue() != null && npc.getSkinSignature() != null) {
                        profile.setProperty(new ProfileProperty("textures", npc.getSkinValue(), npc.getSkinSignature()));
                    }
                    sendPlayerPackets(player, npc, profile, entityId, uuid);
                } else {
                    sendMobPackets(player, npc, entityId, uuid, type);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void hideFromPlayer(Player player, NPC npc) {
        Integer entityId = npcEntityIds.get(npc.getId());
        if (entityId == null) return;
        if (npcViewers.getOrDefault(npc.getId(), new HashSet<>()).remove(player.getUniqueId())) {
            try {
                Object removePacket = ReflectionUtils.CLIENTBOUND_REMOVE_ENTITIES.getConstructor(int[].class).newInstance(new int[]{entityId});
                ReflectionUtils.sendPacket(player, removePacket);
                UUID uuid = npcUUIDs.get(npc.getId());
                if (uuid != null) {
                    Object infoRemovePacket = ReflectionUtils.CLIENTBOUND_PLAYER_INFO_REMOVE.getConstructor(List.class).newInstance(Collections.singletonList(uuid));
                    ReflectionUtils.sendPacket(player, infoRemovePacket);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void despawnNPC(NPC npc) {
        Integer entityId = npcEntityIds.remove(npc.getId());
        UUID uuid = npcUUIDs.get(npc.getId());
        if (entityId != null) {
            try {
                Object removePacket = ReflectionUtils.CLIENTBOUND_REMOVE_ENTITIES.getConstructor(int[].class).newInstance(new int[]{entityId});
                Object infoRemovePacket = uuid != null ? ReflectionUtils.CLIENTBOUND_PLAYER_INFO_REMOVE.getConstructor(List.class).newInstance(Collections.singletonList(uuid)) : null;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ReflectionUtils.sendPacket(p, removePacket);
                    if (infoRemovePacket != null) ReflectionUtils.sendPacket(p, infoRemovePacket);
                }
            } catch (Exception ignored) {}
        }
        npcViewers.remove(npc.getId());
        npcUUIDs.remove(npc.getId());
    }

    private void teleportNPC(Player player, NPC npc, Location loc) {
        teleportNPC(player, npc, loc, loc.getYaw(), loc.getPitch());
    }

    private void teleportNPC(Player player, NPC npc, Location loc, float yaw, float pitch) {
        Integer entityId = getEntityId(npc);
        if (entityId == null) return;
        try {
            Object vec3 = ReflectionUtils.VEC3.getConstructor(double.class, double.class, double.class).newInstance(loc.getX(), loc.getY() - (npc.getType() == EntityType.PLAYER ? 0.1 : 0), loc.getZ());
            Object zeroVec = ReflectionUtils.VEC3.getConstructor(double.class, double.class, double.class).newInstance(0,0,0);
            Object posMoveRot = ReflectionUtils.POSITION_MOVE_ROTATION.getConstructor(ReflectionUtils.VEC3, ReflectionUtils.VEC3, float.class, float.class).newInstance(vec3, zeroVec, yaw, pitch);
            Constructor<?> ctor = ReflectionUtils.CLIENTBOUND_TELEPORT_ENTITY.getConstructor(int.class, ReflectionUtils.POSITION_MOVE_ROTATION, Set.class, boolean.class);
            Object packet = ctor.newInstance(entityId, posMoveRot, Collections.emptySet(), false);
            ReflectionUtils.sendPacket(player, packet);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendMobPackets(Player player, NPC npc, int entityId, UUID uuid, EntityType type) throws Exception {
        Location loc = npc.getLocation();
        Object nmsType = getNMSType(type);
        Object zeroVec = ReflectionUtils.VEC3.getConstructor(double.class, double.class, double.class).newInstance(0,0,0);
        Constructor<?> spawnCtor = ReflectionUtils.CLIENTBOUND_ADD_ENTITY.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, ReflectionUtils.getNMSClass("net.minecraft.world.entity.EntityType"), int.class, ReflectionUtils.VEC3, double.class);
        Object spawnPacket = spawnCtor.newInstance(entityId, uuid, loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw(), nmsType, 0, zeroVec, (double)loc.getYaw());
        ReflectionUtils.sendPacket(player, spawnPacket);

        List<Object> values = new ArrayList<>();
        values.add(ReflectionUtils.createDataValue(0, (byte) 0));
        if (isNametagVisible(player, npc)) {
            Object nmsComp = getNMSComponent(npc.getName());
            values.add(ReflectionUtils.createDataValue(2, Optional.of(nmsComp)));
            values.add(ReflectionUtils.createDataValue(3, true));
        } else {
            values.add(ReflectionUtils.createDataValue(2, Optional.empty()));
            values.add(ReflectionUtils.createDataValue(3, false));
        }
        Object metaPacket = ReflectionUtils.CLIENTBOUND_SET_ENTITY_DATA.getConstructor(int.class, List.class).newInstance(entityId, values);
        ReflectionUtils.sendPacket(player, metaPacket);
        
        setupTeam(player, npc);
        updateNPCRotation(player, npc, loc);
        sendEquipmentPackets(player, npc);
    }

    private Object getNMSComponent(String text) throws Exception {
        String json = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<!i>" + text));
        Method fromJSON = ReflectionUtils.CRAFT_CHAT_MESSAGE.getMethod("fromJSON", String.class);
        return fromJSON.invoke(null, json);
    }

    private void sendPlayerPackets(Player player, NPC npc, PlayerProfile profile, int entityId, UUID uuid) throws Exception {
        Object gameProfile = profile.getClass().getMethod("getGameProfile").invoke(profile);
        Object survival = ReflectionUtils.GAME_TYPE.getField("SURVIVAL").get(null);
        Constructor<?>[] ctors = ReflectionUtils.PLAYER_INFO_ENTRY.getDeclaredConstructors();
        if (ctors.length == 0) return;
        Constructor<?> entryCtor = ctors[0];
        entryCtor.setAccessible(true);
        Object entry = entryCtor.newInstance(uuid, gameProfile, true, 0, survival, null, false, 0, null);
        Object addAction = ReflectionUtils.PLAYER_INFO_ACTION.getField("ADD_PLAYER").get(null);
        Object listedAction = ReflectionUtils.PLAYER_INFO_ACTION.getField("UPDATE_LISTED").get(null);
        Object infoPacket = ReflectionUtils.CLIENTBOUND_PLAYER_INFO_UPDATE.getConstructor(EnumSet.class, List.class)
                .newInstance(EnumSet.of((Enum)addAction, (Enum)listedAction), Collections.singletonList(entry));
        ReflectionUtils.sendPacket(player, infoPacket);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                Location loc = npc.getLocation();
                Constructor<?> spawnCtor = ReflectionUtils.CLIENTBOUND_ADD_ENTITY.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, ReflectionUtils.getNMSClass("net.minecraft.world.entity.EntityType"), int.class, ReflectionUtils.VEC3, double.class);
                Object playerType = ReflectionUtils.getNMSClass("net.minecraft.world.entity.EntityType").getField("PLAYER").get(null);
                Object zeroVec = ReflectionUtils.VEC3.getConstructor(double.class, double.class, double.class).newInstance(0,0,0);
                Object spawnPacket = spawnCtor.newInstance(entityId, uuid, loc.getX(), loc.getY() - 0.1, loc.getZ(), loc.getPitch(), loc.getYaw(), playerType, 0, zeroVec, (double)loc.getYaw());
                ReflectionUtils.sendPacket(player, spawnPacket);

                List<Object> values = new ArrayList<>();
                values.add(ReflectionUtils.createDataValue(16, (byte) (npc.isShowCape() ? 127 : 126)));
                values.add(ReflectionUtils.createDataValue(17, 0.0f));
                if (isNametagVisible(player, npc)) {
                    Object nmsComp = getNMSComponent(npc.getName());
                    values.add(ReflectionUtils.createDataValue(2, Optional.of(nmsComp)));
                    values.add(ReflectionUtils.createDataValue(3, true));
                } else {
                    values.add(ReflectionUtils.createDataValue(2, Optional.empty()));
                    values.add(ReflectionUtils.createDataValue(3, false));
                }
                Object metaPacket = ReflectionUtils.CLIENTBOUND_SET_ENTITY_DATA.getConstructor(int.class, List.class).newInstance(entityId, values);
                ReflectionUtils.sendPacket(player, metaPacket);
                
                setupTeam(player, npc);
                updateNPCRotation(player, npc, loc);
                sendEquipmentPackets(player, npc);
            } catch (Throwable t) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error in sendPlayerPackets task", t);
            }
        }, 2L);
    }

    public void broadcastAnimation(NPC npc, int action) {
        Integer entityId = getEntityId(npc);
        if (entityId == null) return;
        try {
            // In 1.21.1 ClientboundAnimatePacket(int entityId, int action) might not exist or be private
            // It usually takes an Entity object in NMS.
            // For packet-only entities, we sometimes have to use a more manual approach or find the right constructor.
            // Let's try to find any constructor and see if we can make it work, or use a fallback.
            Object packet = null;
            for (Constructor<?> ctor : ReflectionUtils.CLIENTBOUND_ANIMATE.getDeclaredConstructors()) {
                if (ctor.getParameterCount() == 2) {
                    ctor.setAccessible(true);
                    Class<?>[] types = ctor.getParameterTypes();
                    if (types[0] == int.class && types[1] == int.class) {
                        packet = ctor.newInstance(entityId, action);
                        break;
                    }
                }
            }
            
            if (packet == null) return;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (npcViewers.getOrDefault(npc.getId(), Collections.emptySet()).contains(p.getUniqueId())) {
                    ReflectionUtils.sendPacket(p, packet);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void sendEquipmentPackets(Player player, NPC npc) {
        Integer entityId = getEntityId(npc);
        if (entityId == null || npc.getEquipment().isEmpty()) return;
        try {
            List<Object> pairs = new ArrayList<>();
            Method asNMS = ReflectionUtils.CRAFT_ITEM_STACK.getMethod("asNMSCopy", ItemStack.class);
            Method valueOf = ReflectionUtils.NMS_EQUIPMENT_SLOT.getMethod("valueOf", String.class);
            Constructor<?> pairCtor = ReflectionUtils.PAIR.getConstructor(Object.class, Object.class);
            for (Map.Entry<EquipmentSlot, ItemStack> entry : npc.getEquipment().entrySet()) {
                String slotName = getNMSSlotName(entry.getKey());
                if (slotName == null) continue;
                Object nmsSlot = valueOf.invoke(null, slotName);
                Object nmsItem = asNMS.invoke(null, entry.getValue());
                pairs.add(pairCtor.newInstance(nmsSlot, nmsItem));
            }
            if (!pairs.isEmpty()) {
                Object packet = ReflectionUtils.CLIENTBOUND_SET_EQUIPMENT.getConstructor(int.class, List.class).newInstance(entityId, pairs);
                ReflectionUtils.sendPacket(player, packet);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getNMSSlotName(EquipmentSlot slot) {
        switch (slot) {
            case HAND: return "MAINHAND"; case OFF_HAND: return "OFFHAND"; case FEET: return "FEET";
            case LEGS: return "LEGS"; case CHEST: return "CHEST"; case HEAD: return "HEAD";
            default: return null;
        }
    }

    private void setupTeam(Player player, NPC npc) {
        Scoreboard board = player.getScoreboard();
        UUID uuid = npcUUIDs.get(npc.getId());
        if (uuid == null) return;
        
        String entry = npc.getType() == EntityType.PLAYER ? 
            (npc.getName().length() > 16 ? npc.getName().substring(0, 16) : npc.getName()) : 
            uuid.toString();
            
        String teamName = "npc_" + npc.getId().hashCode();
        Team team = board.getTeam(teamName);
        
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }
        
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, npc.isNametagVisible() ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
        team.setOption(Team.Option.COLLISION_RULE, npc.isCollidable() ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
        
        if (!team.hasEntry(entry)) {
            team.addEntry(entry);
        }
    }

    private Integer getEntityId(NPC npc) { return npcEntityIds.get(npc.getId()); }

    private boolean canFit(Location loc, EntityType type) {
        double height = getEntityHeight(type);
        for (double y = 0; y < height; y += 0.5) {
            if (loc.clone().add(0, y, 0).getBlock().getType().isSolid()) return false;
        }
        // Final check for the very top
        if (loc.clone().add(0, height, 0).getBlock().getType().isSolid()) return false;
        return true;
    }

    private double getEntityHeight(EntityType type) {
        switch (type) {
            case PLAYER: case VILLAGER: case ZOMBIE: case SKELETON: return 1.8;
            case CREEPER: return 1.7;
            case IRON_GOLEM: return 2.7;
            case PIG: case COW: return 0.9;
            default: return 1.8;
        }
    }

    private double findGroundY(Location loc) {
        Block b = loc.getBlock();
        if (b.getType().isSolid()) {
            for (int i=1; i<=3; i++) {
                Block above = b.getRelative(0, i, 0);
                if (!above.getType().isSolid()) return above.getLocation().getY();
            }
            return loc.getY();
        }
        for (int i=0; i<=5; i++) {
            Block below = b.getRelative(0, -i, 0);
            if (below.getType().isSolid()) return below.getLocation().getY() + below.getBoundingBox().getHeight();
        }
        return -999;
    }

    public void cleanupPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        activeNPCLocations.remove(uuid);
        for (Set<UUID> viewers : npcViewers.values()) viewers.remove(uuid);
    }

    public void renameNPC(String oldId, String newId) {
        NPC npc = npcs.remove(oldId);
        if (npc == null) return;
        
        Integer entityId = npcEntityIds.remove(oldId);
        UUID uuid = npcUUIDs.remove(oldId);
        Set<UUID> viewers = npcViewers.remove(oldId);
        
        npc.setId(newId);
        npcs.put(newId, npc);
        if (entityId != null) npcEntityIds.put(newId, entityId);
        if (uuid != null) npcUUIDs.put(newId, uuid);
        if (viewers != null) npcViewers.put(newId, viewers);
        
        databaseManager.renameNPC(oldId, newId);
    }

    public void deleteNPC(String id) {
        NPC npc = npcs.remove(id);
        if (npc != null) { 
            despawnNPC(npc); 
            databaseManager.deleteNPC(id);
        }
    }

    public void loadNPCs() {
        // Load from Database
        List<NPC> loaded = databaseManager.loadNPCs();
        if (!loaded.isEmpty()) {
            for (NPC npc : loaded) {
                npcs.put(npc.getId(), npc);
                spawnNPC(npc);
            }
            plugin.getLogger().info("Loaded " + loaded.size() + " NPCs from database.");
            return;
        }

        // Fallback/Migration from YAML
        if (!npcFile.exists()) return;
        plugin.getLogger().info("No NPCs in database. Checking npcs.yml for migration...");
        npcConfig = YamlConfiguration.loadConfiguration(npcFile);
        ConfigurationSection section = npcConfig.getConfigurationSection("npcs");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".name");
            EntityType type = EntityType.valueOf(section.getString(id + ".type", "VILLAGER"));
            Location loc = section.getLocation(id + ".location");
            NPC npc = new NPC(id, name, type, loc);
            npc.setRunAsOp(section.getBoolean(id + ".runAsOp"));
            npc.setRunAsConsole(section.getBoolean(id + ".runAsConsole"));
            npc.setShowCape(section.getBoolean(id + ".showCape", true));
            npc.setCollidable(section.getBoolean(id + ".collidable", true));
            npc.setNpcCollision(section.getBoolean(id + ".npcCollision", false));
            npc.setFlying(section.getBoolean(id + ".flying", false));
            npc.setReturnToSpawn(section.getBoolean(id + ".returnToSpawn", true));
            npc.setNametagVisible(section.getBoolean(id + ".nametagVisible", true));
            npc.setSkinName(section.getString(id + ".skinName"));
            npc.setSkinValue(section.getString(id + ".skinValue"));
            npc.setSkinSignature(section.getString(id + ".skinSignature"));
            String tm = section.getString(id + ".trackingMode", "NONE");
            if (tm.equals("HEAD") || tm.equals("BODY")) npc.setTrackingMode(NPC.TrackingMode.STILL);
            else if (tm.equals("FULL")) npc.setTrackingMode(NPC.TrackingMode.FOLLOW);
            else try { npc.setTrackingMode(NPC.TrackingMode.valueOf(tm)); } catch (Exception e) { npc.setTrackingMode(NPC.TrackingMode.NONE); }
            npc.setTrackingRange(section.getDouble(id + ".trackingRange", 10.0));
            if (section.isList(id + ".commands")) {
                npc.getCommands().addAll(section.getStringList(id + ".commands"));
            }
            if (section.isConfigurationSection(id + ".equipment")) {
                ConfigurationSection eq = section.getConfigurationSection(id + ".equipment");
                for (String k : eq.getKeys(false)) try { npc.getEquipment().put(EquipmentSlot.valueOf(k), eq.getItemStack(k)); } catch (Exception ignored) {}
            }
            npcs.put(id, npc);
            spawnNPC(npc);
            databaseManager.saveNPC(npc); // Migrate to DB
        }
        plugin.getLogger().info("Migrated NPCs from npcs.yml to database.");
    }

    public void saveNPCs() {
        for (NPC npc : npcs.values()) {
            databaseManager.saveNPC(npc);
        }
    }

    public void saveNPC(NPC npc) {
        databaseManager.saveNPC(npc);
    }

    public Collection<NPC> getAllNPCs() { return npcs.values(); }
    public NPC getNPC(String id) { return npcs.get(id); }
    public File getSkinsFolder() { return skinsFolder; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public NPC getNPCByEntityId(int id) {
        for (Map.Entry<String, Integer> e : npcEntityIds.entrySet()) if (e.getValue() == id) return npcs.get(e.getKey());
        return null;
    }
    public void createNPC(String id, String name, EntityType type, Location loc) {
        NPC npc = new NPC(id, name, type, loc);
        npcs.put(id, npc);
        spawnNPC(npc);
        databaseManager.saveNPC(npc);
    }
}