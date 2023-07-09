package com.runicrealms.plugin.config;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.runicrealms.plugin.Npc;
import com.runicrealms.plugin.NpcTag;
import com.runicrealms.plugin.RunicNpcs;
import com.runicrealms.plugin.Skin;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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
                Location location = new Location(
                        Bukkit.getWorld(npcsSection.getString(key + ".location.world")),
                        Double.parseDouble(npcsSection.getString(key + ".location.x")),
                        Double.parseDouble(npcsSection.getString(key + ".location.y")),
                        Double.parseDouble(npcsSection.getString(key + ".location.z")),
                        Float.parseFloat(npcsSection.getString(key + ".location.yaw")),
                        Float.parseFloat(npcsSection.getString(key + ".location.pitch"))
                );

                Hologram hologram = HolographicDisplaysAPI.get(RunicNpcs.getInstance()).createHologram(location.clone().add(0, 2.5, 0));
                hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', "&e" + npcsSection.getString(key + ".name")));
                String color = "";
                String colored = ChatColor.translateAlternateColorCodes('&', npcsSection.getString(key + ".label"));
                if (ChatColor.stripColor(colored).equalsIgnoreCase(colored)) {
                    if (colored.equalsIgnoreCase("Merchant")) {
                        color = "&a";
                    } else if (colored.equalsIgnoreCase("Quest")) {
                        color = "&6";
                    } else {
                        color = "&7";
                    }
                }
                hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', color + npcsSection.getString(key + ".label")));

                ConfigurationSection equipmentSection = npcsSection.getConfigurationSection(key + ".equipment");
                HashMap<EnumWrappers.ItemSlot, ItemStack> equipment = loadEquipment(equipmentSection);


                Npc npc = new Npc(
                        Integer.parseInt(key),
                        location,
                        NpcTag.getFromIdentifier(npcsSection.getString(key + ".label")),
                        npcsSection.getString(key + ".name"),
                        UUID.randomUUID(),
                        new Skin(npcsSection.getString(key + ".skin-texture"), npcsSection.getString(key + ".skin-signature")),
                        hologram,
                        equipment,
                        !npcsSection.contains(key + ".shown") || npcsSection.getBoolean(key + ".shown"));
                npcs.put(Integer.parseInt(key), npc);
            }
        }
        Bukkit.broadcastMessage("Total NPCs Loaded:" + npcs.size());
        RunicNpcs.setNpcs(npcs);
        RunicNpcs.setNpcEntityIds(sortNpcsByEntityId(npcs));
        RunicNpcs.getAPI().placeNpcsInGrid(npcs);
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

    /**
     * Saves a Npc to file storage. Name and label of hologram are set to hologram lines 0 and 1, respectively
     *
     * @param npc    to save
     * @param config the section of the file config
     */
    public static void saveNpc(Npc npc, FileConfiguration config) {
        Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> {
            Location location = npc.hasNewLocation() ? npc.getNewLocation() : npc.getLocation();
            config.set("npcs." + npc.getId() + ".name", npc.getName());
            config.set("npcs." + npc.getId() + ".label", npc.getLabel().getIdentifier());
            config.set("npcs." + npc.getId() + ".location.world", location.getWorld().getName());
            config.set("npcs." + npc.getId() + ".location.x", location.getX());
            config.set("npcs." + npc.getId() + ".location.y", location.getY());
            config.set("npcs." + npc.getId() + ".location.z", location.getZ());
            config.set("npcs." + npc.getId() + ".location.yaw", location.getYaw());
            config.set("npcs." + npc.getId() + ".location.pitch", location.getPitch());
            config.set("npcs." + npc.getId() + ".skin-texture", npc.getSkin().getTexture());
            config.set("npcs." + npc.getId() + ".skin-signature", npc.getSkin().getSignature());

            for(EnumWrappers.ItemSlot slot : npc.getEquipment().keySet()) {
                ItemStack itemStack = npc.getEquipment().get(slot);
                if(itemStack == null) {
                    config.set("npcs." + npc.getId() + ".equipment." + slot.name().toLowerCase() + ".material", "NONE");
                    config.set("npcs." + npc.getId() + ".equipment." + slot.name().toLowerCase() + ".durability", 0);
                } else {
                    config.set("npcs." + npc.getId() + ".equipment." + slot.name().toLowerCase() + ".material", itemStack.getType().toString().toUpperCase());
                    if(itemStack.getItemMeta() instanceof Damageable) {
                        config.set("npcs." + npc.getId() + ".equipment." + slot.name().toLowerCase() + ".durability", ((Damageable) itemStack.getItemMeta()).getDamage());
                    }
                }

            }

            config.set("npcs." + npc.getId() + ".shown", npc.isShown());
            Bukkit.getScheduler().runTaskAsynchronously(RunicNpcs.getInstance(), () -> {
                try {
                    config.save(new File(RunicNpcs.getInstance().getDataFolder(), "npcs.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static void deleteNpc(Integer id, FileConfiguration config) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicNpcs.getInstance(), () -> {
            config.set("npcs." + id, null);
            try {
                config.save(new File(RunicNpcs.getInstance().getDataFolder(), "npcs.yml"));
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
                config.save(new File(RunicNpcs.getInstance().getDataFolder(), "npcs.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    private static HashMap<EnumWrappers.ItemSlot, ItemStack> loadEquipment(ConfigurationSection config) {
        HashMap<EnumWrappers.ItemSlot, ItemStack> equipmentMap = new HashMap<>();

        for(String configSlot : config.getKeys(false)) {
            EnumWrappers.ItemSlot slot = EnumWrappers.ItemSlot.valueOf(configSlot.toUpperCase());
            String matString = config.getString(configSlot + ".material");
            if(matString.equalsIgnoreCase("none")) continue;
            Material material = Material.getMaterial(matString.toUpperCase());
            if(material == null) {
                Bukkit.getLogger().log(Level.WARNING, "Error loading equipment for NPC ID: " + config.getParent());
                equipmentMap.put(slot, null);
                continue;
            }
            ItemStack stack = new ItemStack(material);
            ItemMeta meta = stack.getItemMeta();
            ((Damageable) meta).setDamage(config.getInt(configSlot + ".damage"));
            stack.setItemMeta(meta);
            equipmentMap.put(slot, stack);
        }

        return equipmentMap;
    }

}
