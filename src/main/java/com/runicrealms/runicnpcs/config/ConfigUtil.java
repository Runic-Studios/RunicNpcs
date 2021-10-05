package com.runicrealms.runicnpcs.config;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.NpcHandler;
import com.runicrealms.runicnpcs.Plugin;
import com.runicrealms.runicnpcs.Skin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ConfigUtil {

    public static FileConfiguration getYamlConfigFile(String fileName, File folder) {
        FileConfiguration config;
        File file;
        file = new File(folder, fileName);
        config = new YamlConfiguration();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            config.load(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return config;
    }

    public static void loadNpcs(FileConfiguration config) {
        Map<Integer, Npc> npcs = new HashMap<>();
        if (config.contains("npcs")) {
            ConfigurationSection npcsSection = config.getConfigurationSection("npcs");
            for (String key : npcsSection.getKeys(false)) {
                Hologram hologram = HologramsAPI.createHologram(Plugin.getInstance(), new Location(
                        Bukkit.getWorld(npcsSection.getString(key + ".hologram.world")),
                        Double.parseDouble(npcsSection.getString(key + ".hologram.x")),
                        Double.parseDouble(npcsSection.getString(key + ".hologram.y")),
                        Double.parseDouble(npcsSection.getString(key + ".hologram.z"))));
                hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&e" + npcsSection.getString(key + ".hologram.name")));
                String color = "";
                String colored = ChatColor.translateAlternateColorCodes('&', npcsSection.getString(key + ".hologram.label"));
                if (ChatColor.stripColor(colored).equalsIgnoreCase(colored)) {
                    if (colored.equalsIgnoreCase("Merchant")) {
                        color = "&a";
                    } else if (colored.equalsIgnoreCase("Quest")) {
                        color = "&6";
                    } else {
                        color = "&7";
                    }
                }
                hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', color + npcsSection.getString(key + ".hologram.label")));
                Npc npc = new Npc(
                        new Location(
                                Bukkit.getWorld(npcsSection.getString(key + ".location.world")),
                                Double.parseDouble(npcsSection.getString(key + ".location.x")),
                                Double.parseDouble(npcsSection.getString(key + ".location.y")),
                                Double.parseDouble(npcsSection.getString(key + ".location.z")),
                                Float.parseFloat(npcsSection.getString(key + ".location.yaw")),
                                Float.parseFloat(npcsSection.getString(key + ".location.pitch"))),
                        new Skin(npcsSection.getString(key + ".skin-texture"), npcsSection.getString(key + ".skin-signature")),
                        Integer.parseInt(key),
                        hologram,
                        UUID.randomUUID(),
                        !npcsSection.contains(key + ".shown") || npcsSection.getBoolean(key + ".shown"));
                npcs.put(Integer.parseInt(key), npc);
            }
        }
        Plugin.setNpcs(npcs);
        Plugin.setNpcEntityIds(sortNpcsByEntityId(npcs));
        NpcHandler.placeNpcsInGrid(npcs);
        Bukkit.getLogger().log(Level.INFO, "[RunicNpcs] NPCs have been loaded!");
    }

    /**
     * This sorts our list of Npcs by entity id in order to initialize our entity id map
     *
     * @param npcs our map of npcs from npc id to npc object
     * @return our map of npcs from ENTITY id to npc object
     */
    private static Map<Integer, Npc> sortNpcsByEntityId(Map<Integer, Npc> npcs) {
        Map<Integer, Npc> npcEntityIds = new HashMap<>();
        for (Map.Entry<Integer, Npc> entry : npcs.entrySet()) {
            npcEntityIds.put(entry.getValue().getEntityId(), entry.getValue());
        }
        return npcEntityIds;
    }

    public static void saveNpc(Npc npc, FileConfiguration config) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            config.set("npcs." + npc.getId() + ".hologram.world", npc.getHologram().getLocation().getWorld().getName());
            config.set("npcs." + npc.getId() + ".hologram.x", npc.getHologram().getLocation().getX());
            config.set("npcs." + npc.getId() + ".hologram.y", npc.getHologram().getLocation().getY());
            config.set("npcs." + npc.getId() + ".hologram.z", npc.getHologram().getLocation().getZ());
            config.set("npcs." + npc.getId() + ".hologram.name", ChatColor.stripColor(((TextLine) npc.getHologram().getLine(0)).getText()));
            config.set("npcs." + npc.getId() + ".hologram.label", ChatColor.stripColor(((TextLine) npc.getHologram().getLine(1)).getText()));
            config.set("npcs." + npc.getId() + ".location.world", npc.getLocation().getWorld().getName());
            config.set("npcs." + npc.getId() + ".location.x", npc.getLocation().getX());
            config.set("npcs." + npc.getId() + ".location.y", npc.getLocation().getY());
            config.set("npcs." + npc.getId() + ".location.z", npc.getLocation().getZ());
            config.set("npcs." + npc.getId() + ".location.yaw", npc.getLocation().getYaw());
            config.set("npcs." + npc.getId() + ".location.pitch", npc.getLocation().getPitch());
            config.set("npcs." + npc.getId() + ".skin-texture", npc.getSkin().getTexture());
            config.set("npcs." + npc.getId() + ".skin-signature", npc.getSkin().getSignature());
            config.set("npcs." + npc.getId() + ".uuid", npc.getUuid());
            config.set("npcs." + npc.getId() + ".shown", npc.isShown());
            try {
                config.save(new File(Plugin.getInstance().getDataFolder(), "npcs.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void deleteNpc(Integer id, FileConfiguration config) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            config.set("npcs." + id, null);
            try {
                config.save(new File(Plugin.getInstance().getDataFolder(), "npcs.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static Integer loadNextId(FileConfiguration config) {
        if (config.contains("next-id")) {
            return config.getInt("next-id");
        } else {
            config.set("next-id", 0);
            try {
                config.save(new File(Plugin.getInstance().getDataFolder(), "npcs.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

}
