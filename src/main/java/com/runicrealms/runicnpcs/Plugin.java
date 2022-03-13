package com.runicrealms.runicnpcs;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.runicrealms.runicnpcs.command.RunicNpcCommand;
import com.runicrealms.runicnpcs.config.ConfigUtil;
import com.runicrealms.runicnpcs.event.EventNpcInteract;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Plugin extends JavaPlugin {

    public static double HOLOGRAM_VERTICAL_OFFSET = 2.5; // Blocks

    private static Plugin instance;

    private static Map<Integer, Npc> npcs = new HashMap<>(); // maps NPC ids to npcs
    private static Map<Integer, Npc> npcEntities = new HashMap<>(); // maps ENTITY ids to npcs
    private static Set<UUID> npcEntityUUIDs = new HashSet<>();
    private static FileConfiguration config;
    private static Integer nextId;
    private static PaperCommandManager commandManager;
    private static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;
        commandManager = new PaperCommandManager(this);
        protocolManager = ProtocolLibrary.getProtocolManager();
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
                RunicRestartApi.markPluginLoaded("npcs");
                Bukkit.getScheduler().scheduleAsyncRepeatingTask(Plugin.this, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (NpcHandler.hasLoadedDataForPlayer(player)) {
                            NpcHandler.updateNpcsForPlayer(player);
                        }
                    }
                }, 4 * 20, 4 * 20);
            });
        });
    }

    @Override
    public void onDisable() {
        for (Map.Entry<Integer, Npc> npc : npcs.entrySet()) {
            npc.getValue().delete(false);
        }
    }

    public static void updateNpcs() {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                NpcHandler.updateNpcsForPlayer(player);
            }
        });
    }

    public static Map<Integer, Npc> getNpcs() {
        return npcs;
    }

    public static Map<Integer, Npc> getNpcEntities() {
        return npcEntities;
    }

    public static Set<UUID> getNpcEntityUUIDs() {
        return npcEntityUUIDs;
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public static Integer getNextId() {
        final Integer current = nextId;
        final Integer next = ++nextId;
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> getFileConfig().set("next-id", next));
        return current;
    }

    /**
     * This initializes our map of NPC IDS to our npc objects.
     * This is different from our map of ENTITY IDS.
     *
     * @param npcs maps our npc ids to our npc objects
     */
    public static void setNpcs(Map<Integer, Npc> npcs) {
        Plugin.npcs = npcs;
    }

    /**
     * This initializes our map of ENTITY IDS to our npc objects.
     * This is different from our map of NPC IDS.
     *
     * @param npcEntities maps our entity ids to our npc objects
     */
    public static void setNpcEntityIds(Map<Integer, Npc> npcEntities) {
        Plugin.npcEntities = npcEntities;
    }

    public static FileConfiguration getFileConfig() {
        return config;
    }

}
