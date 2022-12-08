package com.runicrealms.runicnpcs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.runicnpcs.api.RunicNpcsAPI;
import com.runicrealms.runicnpcs.config.ConfigUtil;
import com.runicrealms.runicnpcs.grid.GridBounds;
import com.runicrealms.runicnpcs.grid.MultiWorldGrid;
import com.runicrealms.runicnpcs.listener.ScoreboardHandler;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
                    npc.rotateHeadForPlayer(player);
                }
            }
        }, 100L, 100L);
    }

    @Override
    public Npc createNpc(Location location, String name, String label, String skinId, boolean shown) {
        Skin skin = MineskinUtil.getMineskinSkin(skinId);
        if (skin != null) {
            Hologram hologram = HologramsAPI.createHologram(RunicNpcs.getInstance(), new Location(location.getWorld(), location.getX(), location.getY() + RunicNpcs.HOLOGRAM_VERTICAL_OFFSET, location.getZ()));
            hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&e" + name.replaceAll("_", " ")));
            hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&',
                    (label.equalsIgnoreCase("Merchant") ? "&a" : (label.equalsIgnoreCase("Quest") ? "&6" : "&7")) +
                            label.replaceAll("_", " ")));
            Integer id = RunicNpcs.getNextId();
            Npc npc = new Npc(location, skin, id, hologram, UUID.randomUUID(), shown);
            ConfigUtil.saveNpc(npc, RunicNpcs.getFileConfig());
            RunicNpcs.getNpcs().put(npc.getId(), npc);
            RunicNpcs.getNpcEntities().put(npc.getEntityId(), npc);
            this.createNpcForPlayers(npc);
            this.placeNpcInGrid(npc);
            ScoreboardHandler.addNpcName(npc);
            RunicNpcs.updateNpcs();
            Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(ScoreboardHandler::sendScoreboardPackets));
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
            ScoreboardHandler.removeNpcName(npc);
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
    public boolean isNpc(EntityPlayer entityPlayer) {
        return RunicNpcs.getNpcEntities().containsKey(entityPlayer.getId());
    }

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
        if (!hasLoadedDataForPlayer(player))
            throw new IllegalStateException("Cannot update NPCs before data is loaded for player!");
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

    @EventHandler(priority = EventPriority.HIGHEST) // runs late
    public void onCharacterSelect(CharacterSelectEvent event) {
        HashMap<Npc, Boolean> npcs = new HashMap<>();
        for (Map.Entry<Integer, Npc> entry : RunicNpcs.getNpcEntities().entrySet()) {
            npcs.put(entry.getValue(), false);
        }
        LOADED_NPCS.put(event.getPlayer(), npcs);
        updateNpcsForPlayer(event.getPlayer());
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

}
