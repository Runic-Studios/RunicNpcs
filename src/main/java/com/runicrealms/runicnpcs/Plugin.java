package com.runicrealms.runicnpcs;

import com.runicrealms.runicnpcs.command.RunicNpcCommand;
import com.runicrealms.runicnpcs.config.ConfigUtil;
import com.runicrealms.runicnpcs.event.EventNpcInteract;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Plugin extends JavaPlugin {

    private static Plugin instance;

    private static Map<Integer, Npc> npcs = new HashMap<Integer, Npc>();
    private static Map<Integer, Npc> npcEntityIds = new HashMap<Integer, Npc>();
    private static FileConfiguration config;
    private static Integer nextId;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new EventNpcInteract(), this);
        Bukkit.getPluginCommand("runicnpc").setExecutor(new RunicNpcCommand());
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        config = ConfigUtil.getYamlConfigFile("npcs.yml", this.getDataFolder());
        nextId = ConfigUtil.loadNextId(config);
        npcs = ConfigUtil.loadNpcs(config);
        npcEntityIds = ConfigUtil.sortNpcsByEntityId(npcs);
    }

    public static Map<Integer, Npc> getNpcs() {
        return npcs;
    }

    public static Map<Integer, Npc> getNpcEntityIds() {
        return npcEntityIds;
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static Integer getNextId() {
        final Integer current = new Integer(nextId);
        final Integer next = new Integer(++nextId);
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                getFileConfig().set("next-id", next);
            }
        });
        return current;
    }

    public static FileConfiguration getFileConfig() {
        return config;
    }

}
