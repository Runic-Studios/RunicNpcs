package com.runicrealms.plugin.listener;

import com.runicrealms.plugin.Npc;
import com.runicrealms.plugin.RunicNpcs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
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
        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, NPC_NAMES, 3));
        // keeping for legacy purposes, may need to re-enact if packets don't work as expected
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sendScoreboardPackets(event.getPlayer());
    }
}
