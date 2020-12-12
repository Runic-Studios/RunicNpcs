package com.runicrealms.runicnpcs;


import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_16_R3.ScoreboardTeam;
import net.minecraft.server.v1_16_R3.ScoreboardTeamBase;
import org.bukkit.Bukkit;

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.scoreboard.CraftScoreboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreboardHandler implements Listener {

    private static List<String> npcNames = new ArrayList<String>();
    private static ScoreboardTeam team;

    public static void initScoreboard() {
        for (Map.Entry<Integer, Npc> entry : Plugin.getNpcs().entrySet()) {
            npcNames.add(entry.getValue().getEntityPlayer().getName());
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        team = new ScoreboardTeam(((CraftScoreboard) scoreboard).getHandle(), "npcs");
        team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
    }

    public static void addNpcName(Npc npc) {
        npcNames.add(npc.getEntityPlayer().getName());
    }

    public static void removeNpcName(Npc npc) {
        npcNames.remove(npcNames.indexOf(npc.getEntityPlayer().getName()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, npcNames, 3));
    }

}
