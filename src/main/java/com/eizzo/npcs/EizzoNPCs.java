package com.eizzo.npcs;

import com.eizzo.npcs.commands.NPCCommand;
import com.eizzo.npcs.commands.NPCTabCompleter;
import com.eizzo.npcs.listeners.NPCListener;
import com.eizzo.npcs.managers.NPCManager;
import com.eizzo.npcs.gui.NPCGUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EizzoNPCs extends JavaPlugin {
    private static EizzoNPCs instance;
    private NPCManager npcManager;
    private NPCGUI npcGui;
    private NPCListener npcListener;
    private Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().warning("Vault economy not found! NPC rewards will be disabled.");
        }

        this.npcManager = new NPCManager(this);
        this.npcGui = new NPCGUI(this, npcManager);
        this.npcListener = new NPCListener(this, npcManager, npcGui);

        getCommand("npc").setExecutor(new NPCCommand(this, npcManager, npcGui));
        getCommand("npc").setTabCompleter(new NPCTabCompleter(npcManager));
        
        getServer().getPluginManager().registerEvents(npcListener, this);
        getServer().getPluginManager().registerEvents(npcGui, this);

        getLogger().info("EIZZOs-NPCs (Rewrite) has been enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
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

        public Economy getEconomy() { return econ; }

    }

    