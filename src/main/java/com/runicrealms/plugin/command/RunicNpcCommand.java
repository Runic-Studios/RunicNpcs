package com.runicrealms.plugin.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.runicrealms.plugin.*;
import com.runicrealms.plugin.config.ConfigUtil;
import com.runicrealms.plugin.util.ItemUtil;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

@CommandAlias("runicnpc|npc")
@CommandPermission("runic.op")
public class RunicNpcCommand extends BaseCommand {

    private static void sendMessage(CommandSender commandSender, String message) {
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private static void sendHelpMessage(CommandSender commandSender) {
        sendMessage(commandSender, "&2/runicnpc create &a<name> <label> <mineskin-id> &r- Creates an NPC, name & label can have underscores to indicate spaces");
        sendMessage(commandSender, "&2/runicnpc delete &a<npc-id> &r- Deletes an NPC");
        sendMessage(commandSender, "&2/runicnpc id &r- Gives you the NPC ID of the NPC closest to you");
        sendMessage(commandSender, "&2/runicnpc move &a<npc-id> &r- Moves the npc to your current location!");
        sendMessage(commandSender, "&2/runicnpc rename &a<npc-id> <name> &r- Updates the name of the specified npc! Use underscores for spaces.");
        sendMessage(commandSender, "&2/runicnpc skin &a<npc-id> <mineskin-id> &r- Updates the skin of the specified npc!");
        sendMessage(commandSender, "&2/runicnpc equipment &a<npc-id> <item slot> <material> <durability> &r- Updates the equipment of the specified npc!");

    }

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Default
    @CatchUnknown
    @Conditions("is-console-or-op")
    public void onBaseCommand(CommandSender commandSender) {
        sendHelpMessage(commandSender);
    }

    // runicnpc info <npc>

    @Subcommand("info")
    @Conditions("is-op")
    public void onCommandInfo(Player player, String[] args) {
        if (args.length == 1 && RunicNpcs.getNpcs().get(Integer.valueOf(args[0])) != null) {
            Npc npc = RunicNpcs.getNpcs().get(Integer.valueOf(args[0]));
            sendMessage(player, ChatColor.GREEN + "NPC Name: " + ChatColor.stripColor(((TextHologramLine) npc.getHologram().getLines().get(0)).getText()));
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

    @Subcommand("create")
    @Conditions("is-op")
    public void onCreateCommand(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.YELLOW + "Error, incorrect arguments. Usage: runicnpc create <name> <label> <skin>");
            return;
        }
        sendMessage(player, "&aRetrieving skin data...");
        Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> {
            Skin skin = MineskinUtil.getMineskinSkin(args[2]);
            if (skin != null) {
                String name = args[0];
                Location npcLocation = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getY(), player.getLocation().getBlockZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
                Hologram hologram = HolographicDisplaysAPI.get(RunicNpcs.getInstance()).createHologram(new Location(npcLocation.getWorld(), npcLocation.getX(), npcLocation.getY() + RunicNpcs.HOLOGRAM_VERTICAL_OFFSET, npcLocation.getZ()));

                NpcTag npcTag = NpcTag.getFromIdentifier(args[1]);
                if (npcTag == null) {
                    player.sendMessage(ChatColor.YELLOW + "Error, NPC tag was not a valid value");
                    return;
                }

                hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', "&e" + args[0].replaceAll("_", " ")));
                hologram.getLines().appendText(npcTag.getChatColor() + npcTag.getIdentifier());

                Npc npc = RunicNpcs.getAPI().createNpc(npcLocation, name, npcTag, skin, true);

                sendMessage(player, "&aCreated NPC. ID is " + npc.getId() + ".");
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
                int id = Integer.parseInt(args[0]);
                if (RunicNpcs.getNpcs().containsKey(id)) {
                    RunicNpcs.getAPI().deleteNpc(id);
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
        Bukkit.getScheduler().runTaskAsynchronously(RunicNpcs.getInstance(), () -> {
            double closest = -1;
            Npc closestNpc = null;
            for (Map.Entry<Integer, Npc> entry : RunicNpcs.getNpcs().entrySet()) {
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
            double finalClosest = closest;
            Npc finalClosestNpc = closestNpc;
            Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> {
                if (finalClosest != -1) {
                    sendMessage(player, "&aID of NPC \"" + ChatColor.stripColor(((TextHologramLine) finalClosestNpc.getHologram().getLines().get(0)).getText()) + "\" is " + finalClosestNpc.getId() + ".");
                } else {
                    sendMessage(player, "&cThere are no NPCs in the world!");
                }
            });
        });
    }

    // runicnpc move <npc>

    @Subcommand("move|movehere")
    @Conditions("is-op")
    public void onMoveCommand(Player player, String[] args) {
        if (args.length != 1 || !isInt(args[0])) {
            sendHelpMessage(player);
            return;
        }
        int npcId = Integer.parseInt(args[0]);
        Location npcLocation = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getY(), player.getLocation().getBlockZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
        Npc npc = RunicNpcs.getNpcs().get(npcId);
        if (npc == null) {
            sendMessage(player, "&cThat is not a valid NPC id!");
            return;
        }
        Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> {
            npc.setNewLocation(npcLocation);
            sendMessage(player, "&aNpc has been moved! New location will be visible upon /rstop");
            ConfigUtil.saveNpc(npc, RunicNpcs.getFileConfig());
            RunicNpcs.updateNpcs();
        });
    }

    // runicnpc rename <npc> <name>

    @Subcommand("rename")
    @Conditions("is-op")
    public void onRenameCommand(Player player, String[] args) {
        if (args.length == 2) {
            if (args[1] != null) {
                String name = args[1].replaceAll("_", " ");
                Npc npc = RunicNpcs.getNpcs().get(Integer.valueOf(args[0]));
                Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> {
                    npc.setName(name);
                    ConfigUtil.saveNpc(npc, RunicNpcs.getFileConfig());
                    sendMessage(player, "&aNPC name updated!");
                });

                npc.getHologram().getLines().remove(0);
                npc.getHologram().getLines().insertText(0, name);

            } else {
                sendMessage(player, "&cCommand returned invalid.");
            }
        } else {
            sendHelpMessage(player);
        }
    }

    // runicnpc skin <npc> <skin>

    @Subcommand("skin")
    @Conditions("is-op")
    public void onSkinCommand(Player player, String[] args) {
        if (args.length == 2) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicNpcs.getInstance(), () -> {
                Skin skin = MineskinUtil.getMineskinSkin(args[1]);
                if (skin != null) {
                    Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> {
                        Npc npc = RunicNpcs.getNpcs().get(Integer.valueOf(args[0]));
                        npc.setSkin(skin);
                        ConfigUtil.saveNpc(npc, RunicNpcs.getFileConfig());
                        sendMessage(player, "&aNPC skin updated! Updated skin will be visible upon next rstop.");
                    });
                } else {
                    sendMessage(player, "&cSkin invalid. Did you copy the end of the MineSkin URL?");
                }
            });
        } else {
            sendHelpMessage(player);
        }
    }

    // runicnpc equipment <npc> <slot> <material> <durability>

    @Subcommand("equipment")
    @Conditions("is-op")
    public void onEquipmentCommand(Player player, String[] args) {
        if (args.length == 3 || args.length == 4) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicNpcs.getInstance(), () -> {
                EnumWrappers.ItemSlot equipmentSlot;

                try {
                    equipmentSlot = EnumWrappers.ItemSlot.valueOf(args[1].toUpperCase());
                } catch (IllegalArgumentException ex) {
                    sendMessage(player, "&cEquipment Slot Invalid. Valid Slots: MAINHAND, OFFHAND, HEAD, CHEST, LEGS, FEET");
                    return;
                }

                ItemStack itemstack;
                if (args[2].equalsIgnoreCase("none")) {
                    itemstack = null;
                } else {
                    Material material = Material.getMaterial(args[2].toUpperCase());
                    if (material != null) {
                        itemstack = new ItemStack(material);
                        ItemMeta meta = itemstack.getItemMeta();

                        if (meta instanceof Damageable) {
                            Damageable damageable = (Damageable) meta;
                            int damage = 0;
                            if(args.length == 4) {
                                try {
                                    damage = Integer.parseInt(args[3]);
                                } catch (NumberFormatException ex) {
                                    sendMessage(player, "&cInvalid Damage Amount");
                                    return;
                                }
                            }
                            damageable.setDamage(damage);
                        }

                        itemstack.setItemMeta(meta);

                        if(!ItemUtil.isEquippable(equipmentSlot, itemstack)) {
                            sendMessage(player, "&cYou cannot equip this item in this slot");
                            return;
                        }

                    } else {
                        sendMessage(player, "&cInvalid Material.");
                        return;
                    }
                }

                Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> {
                    Npc npc = RunicNpcs.getNpcs().get(Integer.valueOf(args[0]));
                    npc.getEquipment().put(equipmentSlot, itemstack);
                    npc.updateEquipment();
                    ConfigUtil.saveNpc(npc, RunicNpcs.getFileConfig());
                    sendMessage(player, "&aNPC equipment updated!");
                });
            });
        } else {
            sendHelpMessage(player);
        }
    }

    // runicnpc tag <npc> <tag>

    @Subcommand("tag|retag")
    @Conditions("is-op")
    public void onTagCommand(Player player, String[] args) {
        if (args.length == 2) {
            if (args[1] != null) {
                NpcTag npcTag = NpcTag.getFromIdentifier(args[1]);
                if (npcTag == null) {
                    sendMessage(player, "&cPlease enter a valid tag");
                    return;
                }
                Npc npc = RunicNpcs.getNpcs().get(Integer.valueOf(args[0]));
                Bukkit.getScheduler().runTaskAsynchronously(RunicNpcs.getInstance(), () -> {
                    npc.setLabel(npcTag);
                    ConfigUtil.saveNpc(npc, RunicNpcs.getFileConfig());
                    sendMessage(player, "&aNPC tag updated!");
                });
                // Update the Hologram
                npc.getHologram().getLines().remove(1);
                npc.getHologram().getLines().insertText(1, npcTag.getChatColor() + npcTag.getIdentifier());

            } else {
                sendMessage(player, "&cCommand returned invalid.");
            }
        } else {
            sendHelpMessage(player);
        }
    }
}