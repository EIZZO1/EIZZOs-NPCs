package com.eizzo.npcs.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionUtils {
    private static final String OBC_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
    
    // NMS Classes
    public static Class<?> ENTITY_PLAYER;
    public static Class<?> PLAYER_CONNECTION;
    public static Class<?> PACKET;
    
    // Packet Classes
    public static Class<?> CLIENTBOUND_PLAYER_INFO_UPDATE;
    public static Class<?> CLIENTBOUND_PLAYER_INFO_REMOVE;
    public static Class<?> CLIENTBOUND_ADD_ENTITY;
    public static Class<?> CLIENTBOUND_SET_ENTITY_DATA;
    public static Class<?> CLIENTBOUND_ROTATE_HEAD;
    public static Class<?> CLIENTBOUND_TELEPORT_ENTITY;
    public static Class<?> CLIENTBOUND_REMOVE_ENTITIES;
    public static Class<?> CLIENTBOUND_SET_EQUIPMENT;
    public static Class<?> CLIENTBOUND_ANIMATE;
    public static Class<?> CLIENTBOUND_ENTITY_EVENT;
    public static Class<?> CLIENTBOUND_HURT_ANIMATION;

    // Inner Classes / Enums
    public static Class<?> PLAYER_INFO_ACTION;
    public static Class<?> PLAYER_INFO_ENTRY;
    public static Class<?> DATA_VALUE;
    public static Class<?> ENTITY_DATA_SERIALIZERS;
    public static Class<?> GAME_TYPE;
    public static Class<?> POSITION_MOVE_ROTATION;
    public static Class<?> VEC3;
    public static Class<?> PAIR;
    public static Class<?> NMS_ITEM_STACK;
    public static Class<?> NMS_EQUIPMENT_SLOT;
    public static Class<?> CRAFT_ITEM_STACK;
    public static Class<?> CRAFT_CHAT_MESSAGE;
    public static Class<?> NMS_COMPONENT;
    public static Class<?> NMS_COMPONENT_SERIALIZER;

    // Methods and Fields
    private static Method SEND_PACKET;
    private static Field CONNECTION_FIELD;

    static {
        try {
            ENTITY_PLAYER = getNMSClass("net.minecraft.server.level.ServerPlayer");
            PLAYER_CONNECTION = getNMSClass("net.minecraft.server.network.ServerGamePacketListenerImpl");
            PACKET = getNMSClass("net.minecraft.network.protocol.Packet");
            
            CLIENTBOUND_PLAYER_INFO_UPDATE = getNMSClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            CLIENTBOUND_PLAYER_INFO_REMOVE = getNMSClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
            CLIENTBOUND_ADD_ENTITY = getNMSClass("net.minecraft.network.protocol.game.ClientboundAddEntityPacket");
            CLIENTBOUND_SET_ENTITY_DATA = getNMSClass("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket");
            CLIENTBOUND_ROTATE_HEAD = getNMSClass("net.minecraft.network.protocol.game.ClientboundRotateHeadPacket");
            CLIENTBOUND_TELEPORT_ENTITY = getNMSClass("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket");
            CLIENTBOUND_REMOVE_ENTITIES = getNMSClass("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
            CLIENTBOUND_SET_EQUIPMENT = getNMSClass("net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket");
            CLIENTBOUND_ANIMATE = getNMSClass("net.minecraft.network.protocol.game.ClientboundAnimatePacket");
            CLIENTBOUND_ENTITY_EVENT = getNMSClass("net.minecraft.network.protocol.game.ClientboundEntityEventPacket");
            
            try {
                CLIENTBOUND_HURT_ANIMATION = getNMSClass("net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket");
            } catch (Exception ignored) {}

            PLAYER_INFO_ACTION = getNMSClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");
            PLAYER_INFO_ENTRY = getNMSClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry");
            DATA_VALUE = getNMSClass("net.minecraft.network.syncher.SynchedEntityData$DataValue");
            ENTITY_DATA_SERIALIZERS = getNMSClass("net.minecraft.network.syncher.EntityDataSerializers");
            GAME_TYPE = getNMSClass("net.minecraft.world.level.GameType");
            POSITION_MOVE_ROTATION = getNMSClass("net.minecraft.world.entity.PositionMoveRotation");
            VEC3 = getNMSClass("net.minecraft.world.phys.Vec3");
            
            try {
                PAIR = Class.forName("com.mojang.datafixers.util.Pair");
            } catch (Exception ignored) {}
            
            NMS_ITEM_STACK = getNMSClass("net.minecraft.world.item.ItemStack");
            NMS_EQUIPMENT_SLOT = getNMSClass("net.minecraft.world.entity.EquipmentSlot");
            CRAFT_ITEM_STACK = getOBCClass("inventory.CraftItemStack");
            CRAFT_CHAT_MESSAGE = getOBCClass("util.CraftChatMessage");

            try {
                NMS_COMPONENT = getNMSClass("net.minecraft.network.chat.Component");
                try {
                    NMS_COMPONENT_SERIALIZER = getNMSClass("net.minecraft.network.chat.Component$Serializer");
                } catch (Exception e) {
                    for (Class<?> child : NMS_COMPONENT.getDeclaredClasses()) {
                        if (child.getSimpleName().equals("Serializer")) {
                            NMS_COMPONENT_SERIALIZER = child;
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}

            CONNECTION_FIELD = ENTITY_PLAYER.getField("connection");
            SEND_PACKET = PLAYER_CONNECTION.getMethod("send", PACKET);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    public static Class<?> getOBCClass(String name) throws ClassNotFoundException {
        return Class.forName(OBC_PACKAGE + "." + name);
    }

    public static void sendPacket(Player player, Object packet) {
        if (packet == null || CONNECTION_FIELD == null || SEND_PACKET == null) return;
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = CONNECTION_FIELD.get(handle);
            SEND_PACKET.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setField(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            try {
                Field field = target.getClass().getField(name);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
    
    public static void setFieldByType(Object target, Class<?> type, Object value) {
        try {
            for (Field f : target.getClass().getDeclaredFields()) {
                if (f.getType().equals(type)) {
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object createDataValue(int index, Object value) {
        try {
            Object serializer = null;
            if (value instanceof Byte) serializer = ENTITY_DATA_SERIALIZERS.getField("BYTE").get(null);
            else if (value instanceof Float) serializer = ENTITY_DATA_SERIALIZERS.getField("FLOAT").get(null);
            else if (value instanceof Integer) serializer = ENTITY_DATA_SERIALIZERS.getField("INT").get(null);
            else if (value instanceof Optional) serializer = ENTITY_DATA_SERIALIZERS.getField("OPTIONAL_COMPONENT").get(null);
            else if (value instanceof Boolean) serializer = ENTITY_DATA_SERIALIZERS.getField("BOOLEAN").get(null);
            
            if (serializer == null) return null;
            
            Constructor<?>[] ctors = DATA_VALUE.getDeclaredConstructors();
            if (ctors.length == 0) return null;
            Constructor<?> ctor = ctors[0];
            ctor.setAccessible(true);
            return ctor.newInstance(index, serializer, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object instantiate(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            try {
                Class<?> rfClass = Class.forName("sun.reflect.ReflectionFactory");
                Object rf = rfClass.getMethod("getReflectionFactory").invoke(null);
                Constructor<?> objDef = Object.class.getDeclaredConstructor();
                Constructor<?> intConstr = (Constructor<?>) rfClass.getMethod("newConstructorForSerialization", Class.class, Constructor.class)
                        .invoke(rf, clazz, objDef);
                return clazz.cast(intConstr.newInstance());
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }
}