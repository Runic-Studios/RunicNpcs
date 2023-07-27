package com.runicrealms.plugin.npcs.api;

import com.runicrealms.plugin.npcs.Npc;
import com.runicrealms.plugin.npcs.NpcTag;
import com.runicrealms.plugin.npcs.Skin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;

public interface RunicNpcsAPI {

    /**
     * SHOULD NOT be run on main thread!! Creates a new RunicNpc
     *
     * @param location where the npc will be stationed
     * @param name     the hologram display name
     * @param npcTag   of the npc hologram e.g. "merchant"
     * @param skin   the mine skin id
     * @param shown    whether the npc should be displayed
     * @return an npc object
     */
    Npc createNpc(Location location, String name, NpcTag npcTag, Skin skin, boolean shown);

    /**
     * Adds the player to the loaded npc list
     *
     * @param npc to be added
     */
    void createNpcForPlayers(Npc npc);

    /**
     * @param id of the npc to delete
     */
    void deleteNpc(Integer id);

    /**
     * @return a list of loaded npcs for the player
     */
    Map<Player, Map<Npc, Boolean>> getLoadedNpcs();

    /**
     * @param id of the npc to lookup
     * @return an npc object
     */
    Npc getNpcById(Integer id);

    /**
     * @param player to check
     * @return true if the loaded npcs map has a key matching player
     */
    boolean hasLoadedDataForPlayer(Player player);

    /**
     * @param entity to check
     * @return true if the entity is contained in the npc collection
     */
    boolean isNpc(Entity entity);

    /**
     * Places a npc in the virtual grid
     *
     * @param npc to place
     */
    void placeNpcInGrid(Npc npc);

    /**
     * Places multiple npcs in the grid
     *
     * @param npcs to place
     */
    void placeNpcsInGrid(Map<Integer, Npc> npcs);

    /**
     * Stops a npc from being shown to players
     *
     * @param npc to stop showing
     */
    void removeNpcForPlayers(Npc npc);

    /**
     * Used during npc delete to remove npc from grid
     *
     * @param npc to remove
     */
    void removeNpcFromGrid(Npc npc);

    /**
     * Updates the list of Npcs which should be shown to player
     *
     * @param player to update shown npcs for
     */
    void updateNpcsForPlayer(Player player);

}
