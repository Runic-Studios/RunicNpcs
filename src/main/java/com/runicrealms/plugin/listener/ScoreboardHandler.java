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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        PacketContainer createTeamPacket = createCreateTeamPacket();
        PacketContainer addPlayersPacket = createAddPlayersPacket();

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTeamPacket);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, addPlayersPacket);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sendScoreboardPackets(event.getPlayer());
    }

    private static PacketContainer createCreateTeamPacket() {
        PacketContainer scoreboardTeamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        scoreboardTeamPacket.getStrings().write(0, teamName);
        return scoreboardTeamPacket;
    }

    private static PacketContainer createAddPlayersPacket() {
        PacketContainer scoreboardTeamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);

        scoreboardTeamPacket.getStrings().write(0, teamName);
        StructureModifier<Collection<String>> modifier = scoreboardTeamPacket.getModifier().withType(Collection.class);
        modifier.write(0, NPC_NAMES);
        scoreboardTeamPacket.getIntegers().write(0, 3); // PackOption 3 represents creating a new team

        return scoreboardTeamPacket;
    }
}
