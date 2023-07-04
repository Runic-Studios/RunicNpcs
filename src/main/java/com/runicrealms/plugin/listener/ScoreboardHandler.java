package com.runicrealms.plugin.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.runicrealms.plugin.Npc;
import com.runicrealms.plugin.RunicNpcs;
import com.runicrealms.plugin.api.NpcClickEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardHandler implements Listener {

        public static void updateScoreboard(Player player) {
            Team team;
            if ((team = player.getScoreboard().getTeam("npcs")) != null) {
                team.unregister();
            }

            team = player.getScoreboard().registerNewTeam("npcs");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            for(Npc npc : RunicNpcs.getNpcs().values()) { team.addEntry(npc.getEntityName()); }
        }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterLoadedEvent event) {
            updateScoreboard(event.getPlayer());
    }
}
