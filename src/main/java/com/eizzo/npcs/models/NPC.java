package com.eizzo.npcs.models;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import java.util.*;
public class NPC {
    private String id;
    private String name;
    private EntityType type;
    private Location location;
    private final List<String> commands = new ArrayList<>();
    private final Map<String, List<String>> dialogues = new HashMap<>();
    private boolean runAsOp = false;
    private boolean runAsConsole = false;
    private boolean showCape = true;
    private boolean collidable = true;
    private boolean npcCollision = false;
    private boolean flying = false;
    private boolean hostile = false;
    private boolean returnToSpawn = true;
    private boolean nametagVisible = true;
    private boolean godMode = true;
    private int respawnDelay = 5;
    private double maxHealth = 20.0;
    private double currentHealth = 20.0;
    private boolean showHealthBar = true;
    private double vaultReward = 0.0;
    private double tokenReward = 0.0;
    private String rewardTokenId = "tokens";
    private boolean dialogueOnce = false;
    private int rewardLimit = 0;
    private TrackingMode trackingMode = TrackingMode.NONE;
    private double trackingRange = 10.0;
    private String interactSound;
    private UUID entityUuid;
    private String skinName;
    private String skinValue;
    private String skinSignature;
    private final Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();
    public enum TrackingMode {
        NONE, STILL, FOLLOW
    }

    public NPC(String id, String name, EntityType type, Location location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EntityType getType() { return type; }
    public void setType(EntityType type) { this.type = type; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public List<String> getCommands() { return commands; }
    public Map<String, List<String>> getDialogues() { return dialogues; }
    public boolean isRunAsOp() { return runAsOp; }
    public void setRunAsOp(boolean runAsOp) { this.runAsOp = runAsOp; }
    public boolean isRunAsConsole() { return runAsConsole; }
    public void setRunAsConsole(boolean runAsConsole) { this.runAsConsole = runAsConsole; }
    public boolean isShowCape() { return showCape; }
    public void setShowCape(boolean showCape) { this.showCape = showCape; }
    public boolean isCollidable() { return collidable; }
    public void setCollidable(boolean collidable) { this.collidable = collidable; }
    public boolean isNpcCollision() { return npcCollision; }
    public void setNpcCollision(boolean npcCollision) { this.npcCollision = npcCollision; }
    public boolean isFlying() { return flying; }
    public void setFlying(boolean flying) { this.flying = flying; }
    public boolean isHostile() { return hostile; }
    public void setHostile(boolean hostile) { this.hostile = hostile; }
    public boolean isReturnToSpawn() { return returnToSpawn; }
    public void setReturnToSpawn(boolean returnToSpawn) { this.returnToSpawn = returnToSpawn; }
    public boolean isNametagVisible() { return nametagVisible; }
    public void setNametagVisible(boolean nametagVisible) { this.nametagVisible = nametagVisible; }
    public boolean isGodMode() { return godMode; }
    public void setGodMode(boolean godMode) { this.godMode = godMode; }
    public int getRespawnDelay() { return respawnDelay; }
    public void setRespawnDelay(int respawnDelay) { this.respawnDelay = respawnDelay; }
    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }
    public double getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(double currentHealth) { this.currentHealth = currentHealth; }
    public boolean isShowHealthBar() { return showHealthBar; }
    public void setShowHealthBar(boolean showHealthBar) { this.showHealthBar = showHealthBar; }
    public double getVaultReward() { return vaultReward; }
    public void setVaultReward(double vaultReward) { this.vaultReward = vaultReward; }
    public double getTokenReward() { return tokenReward; }
    public void setTokenReward(double tokenReward) { this.tokenReward = tokenReward; }
    public String getRewardTokenId() { return rewardTokenId; }
    public void setRewardTokenId(String rewardTokenId) { this.rewardTokenId = rewardTokenId; }
    public boolean isDialogueOnce() { return dialogueOnce; }
    public void setDialogueOnce(boolean dialogueOnce) { this.dialogueOnce = dialogueOnce; }
    public int getRewardLimit() { return rewardLimit; }
    public void setRewardLimit(int rewardLimit) { this.rewardLimit = rewardLimit; }
    public TrackingMode getTrackingMode() { return trackingMode; }
    public void setTrackingMode(TrackingMode trackingMode) { this.trackingMode = trackingMode; }
    public double getTrackingRange() { return trackingRange; }
    public void setTrackingRange(double trackingRange) { this.trackingRange = trackingRange; }
    public String getInteractSound() { return interactSound; }
    public void setInteractSound(String interactSound) { this.interactSound = interactSound; }
    public UUID getEntityUuid() { return entityUuid; }
    public void setEntityUuid(UUID entityUuid) { this.entityUuid = entityUuid; }
    public String getSkinName() { return skinName; }
    public void setSkinName(String skinName) { this.skinName = skinName; }
    public String getSkinValue() { return skinValue; }
    public void setSkinValue(String skinValue) { this.skinValue = skinValue; }
    public String getSkinSignature() { return skinSignature; }
    public void setSkinSignature(String skinSignature) { this.skinSignature = skinSignature; }
    public Map<EquipmentSlot, ItemStack> getEquipment() { return equipment; }
}
