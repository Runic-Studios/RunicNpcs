package com.runicrealms.plugin;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.runicrealms.plugin.api.RunicNpcsAPI;
import com.runicrealms.plugin.command.RunicNpcCommand;
import com.runicrealms.plugin.config.ConfigUtil;
import com.runicrealms.plugin.event.EventNpcInteract;
import com.runicrealms.plugin.listener.ScoreboardHandler;
import com.runicrealms.runicrestart.RunicRestart;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RunicNpcs extends JavaPlugin {

    private static final Set<UUID> npcEntityUUIDs = new HashSet<>();
    public static double HOLOGRAM_VERTICAL_OFFSET = 2.5; // Blocks
    private static RunicNpcs instance;
    private static Map<Integer, Npc> npcs = new HashMap<>(); // maps NPC ids to npcs
    private static Map<Integer, Npc> npcEntities = new HashMap<>(); // maps ENTITY ids to npcs
    private static FileConfiguration config;
    private static Integer nextId;
    private static PaperCommandManager commandManager;
    private static ProtocolManager protocolManager;
    private static RunicNpcsAPI runicNpcsAPI;

    public static void updateNpcs() {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                RunicNpcs.getAPI().updateNpcsForPlayer(player);
            }
        });
    }

    public static Map<Integer, Npc> getNpcs() {
        return npcs;
    }

    /**
     * This initializes our map of NPC IDS to our npc objects.
     * This is different from our map of ENTITY IDS.
     *
     * @param npcs maps our npc ids to our npc objects
     */
    public static void setNpcs(Map<Integer, Npc> npcs) {
        RunicNpcs.npcs = npcs;
    }

    public static Map<Integer, Npc> getNpcEntities() {
        return npcEntities;
    }

    public static Set<UUID> getNpcEntityUUIDs() {
        return npcEntityUUIDs;
    }

    public static RunicNpcs getInstance() {
        return instance;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public static RunicNpcsAPI getAPI() {
        return runicNpcsAPI;
    }

    public static Integer getNextId() {
        final Integer current = nextId;
        final Integer next = ++nextId;
        Bukkit.getScheduler().runTaskAsynchronously(RunicNpcs.getInstance(), () -> getFileConfig().set("next-id", next));
        return current;
    }

    /**
     * This initializes our map of ENTITY IDS to our npc objects.
     * This is different from our map of NPC IDS.
     *
     * @param npcEntities maps our entity ids to our npc objects
     */
    public static void setNpcEntityIds(Map<Integer, Npc> npcEntities) {
        RunicNpcs.npcEntities = npcEntities;
    }

    public static FileConfiguration getFileConfig() {
        return config;
    }

    @Override
    public void onDisable() {
        for (Map.Entry<Integer, Npc> npc : npcs.entrySet()) {
            npc.getValue().delete(false);
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        commandManager = new PaperCommandManager(this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        runicNpcsAPI = new NpcHandler();
        Bukkit.getPluginManager().registerEvents(new EventNpcInteract(), this);
        Bukkit.getPluginManager().registerEvents(new NpcHandler(), this);
        Bukkit.getPluginManager().registerEvents(new ScoreboardHandler(), this);
        commandManager.registerCommand(new RunicNpcCommand());
        commandManager.getCommandConditions().addCondition("is-op", (context) -> {
            if (!context.getIssuer().getIssuer().isOp())
                throw new ConditionFailedException("You must be an operator to run this command!");
        });
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            config = ConfigUtil.getYamlConfigFile("npcs.yml", instance.getDataFolder());
            nextId = ConfigUtil.loadNextId(config);
            Bukkit.getScheduler().runTask(instance, () -> {
                ConfigUtil.loadNpcs(config);
                for (Npc npc : npcs.values()) npcEntityUUIDs.add(npc.getUuid());
                ScoreboardHandler.initScoreboard();
                RunicRestart.getAPI().markPluginLoaded("npcs");
                Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (RunicNpcs.getAPI().hasLoadedDataForPlayer(player)) {
                            RunicNpcs.getAPI().updateNpcsForPlayer(player);
                        }
                    }
                }, 4 * 20, 4 * 20);
            });
        });
    }

}
