package com.runicrealms.runicnpcs.config;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.Plugin;
import com.runicrealms.runicnpcs.Skin;
import net.minecraft.server.v1_15_R1.EntityPlayer;
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
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                Map<Integer, Npc> npcs = new HashMap<Integer, Npc>();
                if (config.contains("npcs")) {
                    ConfigurationSection npcsSection = config.getConfigurationSection("npcs");
                    for (String key : npcsSection.getKeys(false)) {
                        Hologram hologram = HologramsAPI.createHologram(Plugin.getInstance(), new Location(
                                Bukkit.getWorld(npcsSection.getString(key + ".hologram.world")),
                                Double.parseDouble(npcsSection.getString(key + ".hologram.x")),
                                Double.parseDouble(npcsSection.getString(key + ".hologram.y")),
                                Double.parseDouble(npcsSection.getString(key + ".hologram.z"))));
                        hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&e" + npcsSection.getString(key + ".hologram.name")));
                        hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&7" + npcsSection.getString(key + ".hologram.label")));
                        String uuid = npcsSection.getString(key + ".uuid");
                        while (Plugin.uuidInUse(uuid)) {
                            uuid = UUID.randomUUID().toString();
                        }
                        npcs.put(Integer.parseInt(key), new Npc(
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
                                    uuid));
                    }
                }
                Plugin.setNpcs(npcs);
                Plugin.setNpcEntityIds(sortNpcsByEntity(npcs));
                Bukkit.getLogger().log(Level.INFO, "[RunicNpcs] NPCs have been loaded!");
            }
        });
    }

    private static Map<EntityPlayer, Npc> sortNpcsByEntity(Map<Integer, Npc> npcs) {
        Map<EntityPlayer, Npc> npcEntityIds = new HashMap<EntityPlayer, Npc>();
        for (Map.Entry<Integer, Npc> entry : npcs.entrySet()) {
            npcEntityIds.put(entry.getValue().getEntityPlayer(), entry.getValue());
        }
        return npcEntityIds;
    }

    public static void saveNpc(Npc npc, FileConfiguration config) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
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
                try {
                    config.save(new File(Plugin.getInstance().getDataFolder(), "npcs.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void deleteNpc(Integer id, FileConfiguration config) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                config.set("npcs." + id, null);
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
