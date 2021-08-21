package com.runicrealms.runicnpcs.api;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.runicnpcs.*;
import com.runicrealms.runicnpcs.config.ConfigUtil;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.UUID;

public class RunicNpcsAPI {

    public static boolean isNpc(EntityPlayer player) {
        return Plugin.getNpcEntities().containsKey(player);
    }

    public static Npc getNpcById(Integer id) {
        return Plugin.getNpcs().get(id);
    }

    public static Npc createNpc(Location location, String name, String label, String skinId, boolean shown) { // DON'T RUN ON MAIN THREAD!!!!
        Skin skin = MineskinUtil.getMineskinSkin(skinId);
        if (skin != null) {
            String uuid = UUID.randomUUID().toString();
            while (Plugin.uuidInUse(uuid)) {
                uuid = UUID.randomUUID().toString();
            }
            final String finalUuid = new String(uuid);
            Hologram hologram = HologramsAPI.createHologram(Plugin.getInstance(), new Location(location.getWorld(), location.getX(), location.getY() + 2.5, location.getZ()));
            hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&e" + name.replaceAll("_", " ")));
            hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&',
                    (label.equalsIgnoreCase("Merchant") ? "&a" : (label.equalsIgnoreCase("Quest") ? "&6" : "&7")) +
                            label.replaceAll("_", " ")));
            Integer id = Plugin.getNextId();
            Npc npc = new Npc(location, skin, id, hologram, finalUuid, shown);
            ConfigUtil.saveNpc(npc, Plugin.getFileConfig());
            Plugin.getNpcs().put(npc.getId(), npc);
            Plugin.getNpcEntities().put(npc.getEntityPlayer(), npc);
            NpcHandler.createNpcForPlayers(npc);
            NpcHandler.placeNpcInGrid(npc);
            ScoreboardHandler.addNpcName(npc);
            Plugin.updateNpcs();
            return npc;
        } else {
            throw new IllegalArgumentException("Invalid skin ID!");
        }
    }

    public static void deleteNpc(Integer id) {
        if (Plugin.getNpcs().containsKey(id)) {
            Npc npc = Plugin.getNpcs().get(id);
            Plugin.getNpcs().remove(id);
            Plugin.getNpcEntities().remove(npc.getEntityPlayer());
            NpcHandler.removeNpcForPlayers(npc);
            NpcHandler.removeNpcFromGrid(npc);
            ScoreboardHandler.removeNpcName(npc);
            npc.delete();
            Plugin.updateNpcs();
            ConfigUtil.deleteNpc(id, Plugin.getFileConfig());
        } else {
            throw new IllegalArgumentException("That NPC ID does not exist!");
        }
    }

}
