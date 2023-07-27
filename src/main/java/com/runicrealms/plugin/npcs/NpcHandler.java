package com.runicrealms.plugin.npcs;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.runicrealms.plugin.npcs.api.RunicNpcsAPI;
import com.runicrealms.plugin.npcs.listener.ScoreboardHandler;
import com.runicrealms.plugin.common.util.grid.GridBounds;
import com.runicrealms.plugin.common.util.grid.MultiWorldGrid;
import com.runicrealms.plugin.npcs.config.ConfigUtil;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class NpcHandler implements Listener, RunicNpcsAPI {

    private static final Map<Player, Map<Npc, Boolean>> LOADED_NPCS = new HashMap<>();
    private static final MultiWorldGrid<Npc> grid = new MultiWorldGrid<>(new GridBounds(-4096, -4096, 4096, 4096), (short) 32);

    /**
     * Update NPC heads every few seconds to ensure proper rotation
     */
    public NpcHandler() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicNpcs.getInstance(), () -> {
            for (Player player : LOADED_NPCS.keySet()) {
                for (Npc npc : LOADED_NPCS.get(player).keySet()) {
                    npc.rotateHeadForPlayer(player, npc.getLocation());
                }
            }
        }, 100L, 100L);
    }

    @Override
    public Npc createNpc(Location location, String name, NpcTag npcTag, Skin skin, boolean shown) {
        if (skin != null) {
            Hologram hologram = HolographicDisplaysAPI.get(RunicNpcs.getInstance()).createHologram(new Location(location.getWorld(), location.getX(), location.getY() + RunicNpcs.HOLOGRAM_VERTICAL_OFFSET, location.getZ()));
            hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', "&e" + name.replaceAll("_", " ")));
            hologram.getLines().appendText(npcTag.getChatColor() + npcTag.getIdentifier());
            Integer id = RunicNpcs.getNextId();
            HashMap<EnumWrappers.ItemSlot, ItemStack> equipmentMap = new HashMap<>();
            Arrays.stream(EnumWrappers.ItemSlot.values()).forEach(slot -> equipmentMap.put(slot, null));
            Npc npc = new Npc(id, location, npcTag, name, UUID.randomUUID(), skin, hologram, equipmentMap, shown);
            ConfigUtil.saveNpc(npc, RunicNpcs.getFileConfig());
            RunicNpcs.getNpcs().put(npc.getId(), npc);
            RunicNpcs.getNpcEntities().put(npc.getEntityId(), npc);
            this.createNpcForPlayers(npc);
            this.placeNpcInGrid(npc);
            RunicNpcs.updateNpcs();
            Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(ScoreboardHandler::updateScoreboard));
            return npc;
        } else {
            throw new IllegalArgumentException("Invalid skin ID!");
        }
    }

    public void createNpcForPlayers(Npc npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            LOADED_NPCS.get(player).put(npc, false);
        }
    }

    @Override
    public void deleteNpc(Integer id) {
        if (RunicNpcs.getNpcs().containsKey(id)) {
            Npc npc = RunicNpcs.getNpcs().get(id);
            RunicNpcs.getNpcs().remove(id);
            RunicNpcs.getNpcEntities().remove(npc.getEntityId());
            this.removeNpcForPlayers(npc);
            this.removeNpcFromGrid(npc);
            npc.delete(true);
            RunicNpcs.updateNpcs();
            ConfigUtil.deleteNpc(id, RunicNpcs.getFileConfig());
        } else {
            throw new IllegalArgumentException("That NPC ID does not exist!");
        }
    }

    @Override
    public Map<Player, Map<Npc, Boolean>> getLoadedNpcs() {
        return LOADED_NPCS;
    }

    @Override
    public Npc getNpcById(Integer id) {
        return RunicNpcs.getNpcs().get(id);
    }

    @Override
    public boolean hasLoadedDataForPlayer(Player player) {
        return LOADED_NPCS.containsKey(player);
    }

    @Override
    public boolean isNpc(Entity entity) { return RunicNpcs.getNpcEntities().containsKey(entity.getEntityId()); }

    @Override
    public void placeNpcInGrid(Npc npc) {
        grid.insertElement(npc.getLocation(), npc);
    }

    @Override
    public void placeNpcsInGrid(Map<Integer, Npc> npcs) {
        for (Map.Entry<Integer, Npc> entry : npcs.entrySet()) {
            grid.insertElement(entry.getValue().getLocation(), entry.getValue());
        }
    }

    @Override
    public void removeNpcForPlayers(Npc npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            LOADED_NPCS.get(player).remove(npc);
        }
    }

    @Override
    public void removeNpcFromGrid(Npc npc) {
        if (grid.containsElementInGrid(npc.getLocation(), npc)) {
            grid.removeElement(npc.getLocation(), npc);
            return;
        }
        throw new IllegalArgumentException("Npc not in grid!");
    }

    @Override
    public void updateNpcsForPlayer(Player player) {
        if (!hasLoadedDataForPlayer(player)) {
            //throw new IllegalStateException("Cannot update NPCs before data is loaded for player!");
            HashMap<Npc, Boolean> npcs = new HashMap<>();
            for (Map.Entry<Integer, Npc> entry : RunicNpcs.getNpcEntities().entrySet()) {
                npcs.put(entry.getValue(), false);
            }
            LOADED_NPCS.put(player, npcs);
        }
        Set<Npc> surrounding = grid.getSurroundingElements(player.getLocation(), (short) 2);
        for (Map.Entry<Npc, Boolean> entry : LOADED_NPCS.get(player).entrySet()) {
            if (entry.getValue()) {
                if ((!surrounding.contains(entry.getKey())) || (!entry.getKey().isShown())) {
                    entry.getKey().despawnForPlayer(player);
                    LOADED_NPCS.get(player).put(entry.getKey(), false);
                }
            } else {
                if (surrounding.contains(entry.getKey()) && entry.getKey().isShown()) {
                    entry.getKey().spawnForPlayer(player);
                    LOADED_NPCS.get(player).put(entry.getKey(), true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (RunicNpcs.getNpcEntityUUIDs().contains(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "There was an error logging you in\nTry again later! Error: RNPCS_IN_USE");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        LOADED_NPCS.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (LOADED_NPCS.containsKey(event.getPlayer())) {
            updateNpcsForPlayer(event.getPlayer());
        }
    }

}
