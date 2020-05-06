package com.runicrealms.runicnpcs.command;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.NpcHandler;
import com.runicrealms.runicnpcs.Plugin;
import com.runicrealms.runicnpcs.Skin;
import com.runicrealms.runicnpcs.config.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class RunicNpcCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.isOp()) {
                Player player = (Player) sender;
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("create")) { // runicnpc create <name> <label> <skin>
                        if (args.length == 4) {
                            sendMessage(player, "&aRetrieving skin data...");
                            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    Skin skin = getMineskinSkin(args[3]);
                                    if (skin != null) {
                                        String uuid = UUID.randomUUID().toString();
                                        while (Plugin.uuidInUse(uuid)) {
                                            uuid = UUID.randomUUID().toString();
                                        }
                                        final String finalUuid = new String(uuid);
                                        Bukkit.getScheduler().runTask(Plugin.getInstance(), new Runnable() {
                                            @Override
                                            public void run() {
                                                Location npcLocation = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getBlockY(), player.getLocation().getBlockZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
                                                Hologram hologram = HologramsAPI.createHologram(Plugin.getInstance(), new Location(npcLocation.getWorld(), npcLocation.getX(), npcLocation.getY() + 2.5, npcLocation.getZ()));
                                                hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&e" + args[1].replaceAll("_", " ")));
                                                hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&7" + args[2].replaceAll("_", " ")));
                                                Integer id = new Integer(Plugin.getNextId());
                                                Npc npc = new Npc(npcLocation, skin, id, hologram, finalUuid);
                                                ConfigUtil.saveNpc(npc, Plugin.getFileConfig());
                                                Plugin.getNpcs().put(npc.getId(), npc);
                                                Plugin.getNpcEntities().put(npc.getEntityPlayer(), npc);
                                                NpcHandler.placeNpcInGrid(npc);
                                                Plugin.updateNpcs();
                                                sendMessage(player, "&aCreated NPC. ID is " + id + ".");
                                            }
                                        });
                                    } else {
                                        sendMessage(player, "&cSkin invalid.");
                                    }
                                }
                            });
                        } else {
                            sendHelpMessage(player);
                        }
                    } else if (args[0].equalsIgnoreCase("delete")) {
                        if (args.length == 2) {
                            if (isInt(args[1])) {
                                if (Plugin.getNpcs().containsKey(Integer.parseInt(args[1]))) {
                                    Npc npc = Plugin.getNpcs().get(Integer.parseInt(args[1]));
                                    Plugin.getNpcs().remove(Integer.parseInt(args[1]));
                                    Plugin.getNpcEntities().remove(npc.getEntityPlayer());
                                    NpcHandler.removeNpcFromGrid(npc);
                                    npc.delete();
                                    Plugin.updateNpcs();
                                    ConfigUtil.deleteNpc(Integer.parseInt(args[1]), Plugin.getFileConfig());
                                    sendMessage(player, "&aSuccessfully removed NPC!");
                                } else {
                                    sendMessage(player, "&cThere is no NPC with that ID!");
                                }
                            } else {
                                sendMessage(player, "&cPlease enter the NPC ID, you can retrieve it with /npc id while standing next to it.");
                            }
                        } else {
                            sendHelpMessage(player);
                        }
                    } else if (args[0].equalsIgnoreCase("id")) {
                        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                double closest = -1;
                                Npc closestNpc = null;
                                for (Map.Entry<Integer, Npc> entry : Plugin.getNpcs().entrySet()) {
                                    if (closest == -1) {
                                        closest = player.getLocation().distanceSquared(entry.getValue().getLocation());
                                        closestNpc = entry.getValue();
                                    } else if (player.getLocation().distanceSquared(entry.getValue().getLocation()) < closest) {
                                        closest = player.getLocation().distanceSquared(entry.getValue().getLocation());
                                        closestNpc = entry.getValue();
                                    }
                                }
                                if (closest != -1) {
                                    sendMessage(player, "&aID of NPC \"" + ChatColor.stripColor(((TextLine) closestNpc.getHologram().getLine(0)).getText()) + "\" is " + closestNpc.getId() + ".");
                                } else {
                                    sendMessage(player, "&cThere are no NPCs in the world!");
                                }
                            }
                        });
                    } else {
                        sendHelpMessage(player);
                    }
                } else {
                    sendHelpMessage(player);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            }
        } else {
            sender.sendMessage("This command cannot be run from console!");
        }
        return true;
    }

    private static void sendMessage(Player player, String message){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private static void sendHelpMessage(Player player) {
        sendMessage(player, "&2/runicnpc create &a<name> <label> <mineskin-id> &r- Creates an NPC, name & label can have underscores to indicate spaces");
        sendMessage(player, "&2/runicnpc delete &a<npd-id> &r- Deletes an NPC");
        sendMessage(player, "&2/runicnpc id &r- Gives you the NPC ID of the NPC closest to you");
    }

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static Skin getMineskinSkin(String id) {
        String url = "https://api.mineskin.org/get/id/" + id;
        try {
            Scanner scanner = new Scanner(new URL(url).openStream(), "UTF-8");
            Scanner withDelimiter = scanner.useDelimiter("\\A");
            JSONObject object = (JSONObject) JSONValue.parseWithException(scanner.next());
            JSONObject data = (JSONObject) object.get("data");
            JSONObject texture = (JSONObject) data.get("texture");
            String value = (String) texture.get("value");
            String signature = (String) texture.get("signature");
            scanner.close();
            withDelimiter.close();
            return new Skin(value, signature);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

}
