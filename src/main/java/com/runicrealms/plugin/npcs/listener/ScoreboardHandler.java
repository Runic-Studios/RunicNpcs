package com.runicrealms.plugin.npcs.listener;

import com.runicrealms.plugin.npcs.Npc;
import com.runicrealms.plugin.npcs.RunicNpcs;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

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
