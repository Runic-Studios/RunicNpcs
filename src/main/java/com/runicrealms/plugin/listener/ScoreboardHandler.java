package com.runicrealms.plugin.listener;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.runicrealms.plugin.Npc;
import com.runicrealms.plugin.RunicNpcs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class ScoreboardHandler implements Listener {

    private static final List<String> NPC_NAMES = new ArrayList<>();
    private static String teamName;

    public static void initScoreboard() {
        for (Map.Entry<Integer, Npc> entry : RunicNpcs.getNpcs().entrySet()) {
            NPC_NAMES.add(entry.getValue().getName());
        }
        teamName = "npcs";
    }

    public static void addNpcName(Npc npc) {
        NPC_NAMES.add(npc.getName());
    }

    public static void removeNpcName(Npc npc) {
        NPC_NAMES.remove(npc.getName());
    }

    public static void sendScoreboardPackets(Player player) {
        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, NPC_NAMES, 3));
        PacketContainer teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        teamPacket.getStrings().write(0, teamName);
        teamPacket.getIntegers().write(0, 0);
        teamPacket.getSpecificModifier(Collection.class).write(0, NPC_NAMES);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, teamPacket);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sendScoreboardPackets(event.getPlayer());
    }
}
