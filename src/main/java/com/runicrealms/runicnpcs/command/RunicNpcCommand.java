package com.runicrealms.runicnpcs.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.runicrealms.runicnpcs.*;
import com.runicrealms.runicnpcs.config.ConfigUtil;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

@CommandAlias("runicnpc|npc")
public class RunicNpcCommand extends BaseCommand {

    @Default
    @CatchUnknown
    @Conditions("is-console-or-op")
    public void onBaseCommand(CommandSender commandSender) {
        sendHelpMessage(commandSender);
    }

    @Subcommand("create")
    @Conditions("is-op")
    public void onCreateCommand(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.YELLOW + "Error, incorrect arguments. Usage: runicnpc create <name> <label> <skin>");
            return;
        }
        sendMessage(player, "&aRetrieving skin data...");
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            Skin skin = MineskinUtil.getMineskinSkin(args[2]);
            if (skin != null) {
                Bukkit.getScheduler().runTask(Plugin.getInstance(), () -> {
                    Location npcLocation = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getBlockY(), player.getLocation().getBlockZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
                    Hologram hologram = HologramsAPI.createHologram(Plugin.getInstance(), new Location(npcLocation.getWorld(), npcLocation.getX(), npcLocation.getY() + 2.5, npcLocation.getZ()));
                    hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&e" + args[0].replaceAll("_", " ")));
                    hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&',
                            (args[1].equalsIgnoreCase("Merchant") ? "&a" : (args[1].equalsIgnoreCase("Quest") ? "&6" : "&7")) +
                                    args[1].replaceAll("_", " ")));
                    Integer id = Plugin.getNextId();
                    Npc npc = new Npc(npcLocation, skin, id, hologram, UUID.randomUUID(), true);
                    ConfigUtil.saveNpc(npc, Plugin.getFileConfig());
                    Plugin.getNpcs().put(npc.getId(), npc);
                    Plugin.getNpcEntities().put(npc.getEntityId(), npc);
                    NpcHandler.createNpcForPlayers(npc);
                    NpcHandler.placeNpcInGrid(npc);
                    ScoreboardHandler.addNpcName(npc);
                    Plugin.updateNpcs();
                    ScoreboardHandler.sendScoreboardPackets(player);
                    sendMessage(player, "&aCreated NPC. ID is " + id + ".");
                });
            } else {
                sendMessage(player, "&cSkin invalid.");
            }
        });
    }

    // runicnpc delete <id>
    @Subcommand("delete")
    @Conditions("is-op")
    public void onDeleteCommand(Player player, String[] args) {
        if (args.length == 1) {
            if (isInt(args[0])) {
                if (Plugin.getNpcs().containsKey(Integer.parseInt(args[0]))) {
                    Npc npc = Plugin.getNpcs().get(Integer.parseInt(args[0]));
                    Plugin.getNpcs().remove(Integer.parseInt(args[0]));
                    Plugin.getNpcEntities().remove(npc.getEntityId());
                    NpcHandler.removeNpcForPlayers(npc);
                    NpcHandler.removeNpcFromGrid(npc);
                    ScoreboardHandler.removeNpcName(npc);
                    npc.delete(true);
                    Plugin.updateNpcs();
                    ConfigUtil.deleteNpc(Integer.parseInt(args[0]), Plugin.getFileConfig());
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
    }

    // runicnpc id
    @Subcommand("id")
    @Conditions("is-op")
    public void onIdCommand(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            double closest = -1;
            Npc closestNpc = null;
            for (Map.Entry<Integer, Npc> entry : Plugin.getNpcs().entrySet()) {
                if (player.getLocation().getWorld() == entry.getValue().getLocation().getWorld()) {
                    if (closest == -1) {
                        closest = player.getLocation().distanceSquared(entry.getValue().getLocation());
                        closestNpc = entry.getValue();
                    } else if (player.getLocation().distanceSquared(entry.getValue().getLocation()) < closest) {
                        closest = player.getLocation().distanceSquared(entry.getValue().getLocation());
                        closestNpc = entry.getValue();
                    }
                }
            }
            if (closest != -1) {
                sendMessage(player, "&aID of NPC \"" + ChatColor.stripColor(((TextLine) closestNpc.getHologram().getLine(0)).getText()) + "\" is " + closestNpc.getId() + ".");
            } else {
                sendMessage(player, "&cThere are no NPCs in the world!");
            }
        });
    }

    // runicnpc info <npc>
    @Subcommand("info")
    @Conditions("is-op")
    public void onCommandInfo(Player player, String[] args) {
        if (args.length == 1 && Plugin.getNpcs().get(Integer.valueOf(args[0])) != null) {
            Npc npc = Plugin.getNpcs().get(Integer.valueOf(args[0]));
            sendMessage(player, ChatColor.GREEN + "NPC Name: " + ChatColor.stripColor(((TextLine) npc.getHologram().getLine(0)).getText()));
            sendMessage(player,
                    ChatColor.GREEN + "NPC Location: " +
                            npc.getLocation().getX() + "x " +
                            npc.getLocation().getY() + "y " +
                            npc.getLocation().getZ() + "z"
            );
        } else {
            sendHelpMessage(player);
        }
    }

    // runicnpc skin <npc> <skin>
    @Subcommand("skin")
    @Conditions("is-op")
    public void onSkinCommand(Player player, String[] args) {
        if (args.length == 2) {
            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
                Skin skin = MineskinUtil.getMineskinSkin(args[1]);
                if (skin != null) {
                    Bukkit.getScheduler().runTask(Plugin.getInstance(), () -> {
                        Npc npc = Plugin.getNpcs().get(Integer.valueOf(args[0]));
                        npc.setSkin(skin);
                        ConfigUtil.saveNpc(npc, Plugin.getFileConfig());
                        sendMessage(player, "&aNPC skin updated! Updated skin will be visible upon next rstop.");
                    });
                } else {
                    sendMessage(player, "&cSkin invalid.");
                }
            });
        } else {
            sendHelpMessage(player);
        }
    }

    private static void sendMessage(CommandSender commandSender, String message) {
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private static void sendHelpMessage(CommandSender commandSender) {
        sendMessage(commandSender, "&2/runicnpc create &a<name> <label> <mineskin-id> &r- Creates an NPC, name & label can have underscores to indicate spaces");
        sendMessage(commandSender, "&2/runicnpc delete &a<npc-id> &r- Deletes an NPC");
        sendMessage(commandSender, "&2/runicnpc id &r- Gives you the NPC ID of the NPC closest to you");
        sendMessage(commandSender, "&2/runicnpc skin &a<npc-id> <mineskin-id> &r- Updates the skin of the specified npc!");
    }

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}