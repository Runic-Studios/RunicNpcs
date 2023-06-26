package com.runicrealms.plugin.listener;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import com.runicrealms.plugin.Npc;
import com.runicrealms.plugin.RunicNpcs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardHandler implements Listener {

    private static String teamName;

    public static void initScoreboard() {
        teamName = "npcs";
    }

    public static void sendScoreboardPackets(Player player) {
        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, NPC_NAMES, 3));
        // keeping for legacy purposes, may need to re-enact if packets don't work as expected
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sendScoreboardPackets(event.getPlayer());
    }
}
