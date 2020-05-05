package com.runicrealms.runicnpcs;

import net.minecraft.server.v1_15_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class NpcHandler implements Listener {

    private static Map<Player, Map<Npc, Boolean>> loadedNpcs = new HashMap<Player, Map<Npc, Boolean>>();

    public static void updateNpcsForPlayer(Player player) {
        for (Map.Entry<Npc, Boolean> entry : loadedNpcs.get(player).entrySet()) {
            if (entry.getValue() == true) {
                if (entry.getKey().getLocation().distanceSquared(player.getLocation()) > 10000) {
                    entry.getKey().despawnForPlayer(player);
                }
            } else {
                if (entry.getKey().getLocation().distanceSquared(player.getLocation()) <= 10000) {
                    entry.getKey().spawnForPlayer(player);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                HashMap<Npc, Boolean> npcs = new HashMap<Npc, Boolean>();
                for (Map.Entry<EntityPlayer, Npc> entry : Plugin.getNpcEntities().entrySet()) {
                    npcs.put(entry.getValue(), false);
                }
                loadedNpcs.put(event.getPlayer(), npcs);
                updateNpcsForPlayer(event.getPlayer());
            }
        }, 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        loadedNpcs.remove(event.getPlayer());
    }

    public static boolean hasLoadedDataForPlayer(Player player) {
        return loadedNpcs.containsKey(player);
    }

}
