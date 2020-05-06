package com.runicrealms.runicnpcs;

import com.runicrealms.runicnpcs.grid.GridBounds;
import com.runicrealms.runicnpcs.grid.GridLocation;
import com.runicrealms.runicnpcs.grid.NpcGrid;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NpcHandler implements Listener {

    private static Map<Player, Map<Npc, Boolean>> loadedNpcs = new HashMap<Player, Map<Npc, Boolean>>();

    private static NpcGrid grid = new NpcGrid(new GridBounds(-4096, -4096, 4096, 4096), (short) 32);

    public static void placeNpcsInGrid(Map<Integer, Npc> npcs) {
        for (Map.Entry<Integer, Npc> entry : npcs.entrySet()) {
            grid.insert(entry.getValue());
        }
    }

    public static void removeNpcFromGrid(Npc npc) {
        GridLocation location = grid.getGridLocationFromLocation(npc.getLocation());
        if (grid.containsElementInGrid(location, npc)) {
            grid.removeElementInGrid(location, npc);
            return;
        }
        throw new IllegalArgumentException("Npc not in grid!");
    }

    public static void placeNpcInGrid(Npc npc) {
        grid.insert(npc);
    }

    public static void updateNpcsForPlayer(Player player) {
        Set<Npc> surrounding = grid.getNearbyNpcs(player.getLocation());
        for (Map.Entry<Npc, Boolean> entry : loadedNpcs.get(player).entrySet()) {
            if (entry.getValue() == true) {
                if (!surrounding.contains(entry.getKey())) {
                    entry.getKey().despawnForPlayer(player);
                }
            } else {
                if (surrounding.contains(entry.getKey())) {
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

    public static NpcGrid getNpcGrid() {
        return grid;
    }

}
