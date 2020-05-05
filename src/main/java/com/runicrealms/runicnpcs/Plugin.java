package com.runicrealms.runicnpcs;

import com.runicrealms.runicnpcs.command.RunicNpcCommand;
import com.runicrealms.runicnpcs.config.ConfigUtil;
import com.runicrealms.runicnpcs.event.EventNpcInteract;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Plugin extends JavaPlugin {

    private static Plugin instance;

    private static Map<Integer, Npc> npcs = new HashMap<Integer, Npc>();
    private static Map<EntityPlayer, Npc> npcEntities = new HashMap<EntityPlayer, Npc>();
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
        ConfigUtil.loadNpcs(config);
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (NpcHandler.hasLoadedDataForPlayer(player)) {
                        NpcHandler.updateNpcsForPlayer(player);
                    }
                }
            }
        }, 7 * 20, 7 * 20);
    }

    @Override
    public void onDisable() {
        for (Map.Entry<Integer, Npc> npc : npcs.entrySet()) {
            npc.getValue().delete();
        }
    }

    public static Map<Integer, Npc> getNpcs() {
        return npcs;
    }

    public static Map<EntityPlayer, Npc> getNpcEntities() {
        return npcEntities;
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

    public static void setNpcs(Map<Integer, Npc> npcs) {
        Plugin.npcs = npcs;
    }

    public static void setNpcEntityIds(Map<EntityPlayer, Npc> npcs) {
        Plugin.npcEntities = npcs;
    }

    public static FileConfiguration getFileConfig() {
        return config;
    }

    public static boolean uuidInUse(String uuid) {
        String url = "https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names";
        try {
            Scanner scanner = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext()) {
                scanner.close();
                return true;
            }
            scanner.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return false;
    }

}
