package com.eizzo.npcs;

import com.eizzo.npcs.commands.NPCCommand;
import com.eizzo.npcs.commands.NPCTabCompleter;
import com.eizzo.npcs.listeners.NPCListener;
import com.eizzo.npcs.managers.NPCManager;
import com.eizzo.npcs.gui.NPCGUI;
import org.bukkit.plugin.java.JavaPlugin;

public class EizzoNPCs extends JavaPlugin {
    private static EizzoNPCs instance;
    private NPCManager npcManager;
    private NPCGUI npcGui;
    private NPCListener npcListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.npcManager = new NPCManager(this);
        this.npcGui = new NPCGUI(this, npcManager);
        this.npcListener = new NPCListener(this, npcManager, npcGui);

        getCommand("npc").setExecutor(new NPCCommand(this, npcManager, npcGui));
        getCommand("npc").setTabCompleter(new NPCTabCompleter(npcManager));
        
        getServer().getPluginManager().registerEvents(npcListener, this);
        getServer().getPluginManager().registerEvents(npcGui, this);

        getLogger().info("EIZZOs-NPCs (Rewrite) has been enabled!");
    }

    @Override
    public void onDisable() {
        if (npcManager != null) {
            npcManager.saveNPCs();
            npcManager.getDatabaseManager().close();
        }
        getLogger().info("EIZZOs-NPCs (Rewrite) has been disabled!");
    }

        public static EizzoNPCs getInstance() { return instance; }

        public NPCGUI getNpcGui() { return npcGui; }

        public NPCListener getNpcListener() { return npcListener; }

    }

    