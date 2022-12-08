package com.runicrealms.runicnpcs.listener;


import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.RunicNpcs;
import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_16_R3.ScoreboardTeam;
import net.minecraft.server.v1_16_R3.ScoreboardTeamBase;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreboardHandler implements Listener {

    private static final List<String> NPC_NAMES = new ArrayList<>();
    private static ScoreboardTeam team;

    public static void initScoreboard() {
        for (Map.Entry<Integer, Npc> entry : RunicNpcs.getNpcs().entrySet()) {
            NPC_NAMES.add(entry.getValue().getEntityPlayer().getName());
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        team = new ScoreboardTeam(((CraftScoreboard) scoreboard).getHandle(), "npcs");
        team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
    }

    public static void addNpcName(Npc npc) {
        NPC_NAMES.add(npc.getEntityPlayer().getName());
    }

    public static void removeNpcName(Npc npc) {
        NPC_NAMES.remove(npc.getEntityPlayer().getName());
    }

    public static void sendScoreboardPackets(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, NPC_NAMES, 3));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sendScoreboardPackets(event.getPlayer());
    }

}
