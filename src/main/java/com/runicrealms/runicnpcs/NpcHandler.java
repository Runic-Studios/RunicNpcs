package com.runicrealms.runicnpcs;

import com.runicrealms.runicnpcs.grid.GridBounds;
import com.runicrealms.runicnpcs.grid.GridLocation;
import com.runicrealms.runicnpcs.grid.MultiWorldGrid;
import net.minecraft.server.v1_16_R3.EntityPlayer;
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

    private static final MultiWorldGrid<Npc> grid = new MultiWorldGrid<Npc>(new GridBounds(-4096, -4096, 4096, 4096), (short) 32);

    public static void placeNpcsInGrid(Map<Integer, Npc> npcs) {
        for (Map.Entry<Integer, Npc> entry : npcs.entrySet()) {
            grid.insertElement(entry.getValue().getLocation(), entry.getValue());
        }
    }

    public static void removeNpcFromGrid(Npc npc) {
        if (grid.containsElementInGrid(npc.getLocation(), npc)) {
            grid.removeElement(npc.getLocation(), npc);
            return;
        }
        throw new IllegalArgumentException("Npc not in grid!");
    }

    public static void createNpcForPlayers(Npc npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadedNpcs.get(player).put(npc, false);
        }
    }

    public static void removeNpcForPlayers(Npc npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadedNpcs.get(player).remove(npc);
        }
    }

    public static void placeNpcInGrid(Npc npc) {
        grid.insertElement(npc.getLocation(), npc);
    }

    public static void updateNpcsForPlayer(Player player) {
        Set<Npc> surrounding = grid.getSurroundingElements(player.getLocation(), (short) 2);
        for (Map.Entry<Npc, Boolean> entry : loadedNpcs.get(player).entrySet()) {
            if (entry.getValue()) {
                if ((!surrounding.contains(entry.getKey())) || (!entry.getKey().isShown())) {
                    entry.getKey().despawnForPlayer(player);
                    loadedNpcs.get(player).put(entry.getKey(), false);
                }
            } else {
                if (surrounding.contains(entry.getKey()) && entry.getKey().isShown()) {
                    entry.getKey().spawnForPlayer(player);
                    loadedNpcs.get(player).put(entry.getKey(), true);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Plugin.getInstance(), () -> {
            HashMap<Npc, Boolean> npcs = new HashMap<Npc, Boolean>();
            for (Map.Entry<EntityPlayer, Npc> entry : Plugin.getNpcEntities().entrySet()) {
                npcs.put(entry.getValue(), false);
            }
            loadedNpcs.put(event.getPlayer(), npcs);
            updateNpcsForPlayer(event.getPlayer());
        }, 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        loadedNpcs.remove(event.getPlayer());
    }

    public static boolean hasLoadedDataForPlayer(Player player) {
        return loadedNpcs.containsKey(player);
    }

    public static MultiWorldGrid<Npc> getNpcGrid() {
        return grid;
    }

}
