package com.runicrealms.plugin.api;

import com.runicrealms.plugin.Npc;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NpcClickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Npc npc;
    private final Player player;

    public NpcClickEvent(Npc npc, Player player) {
        this.npc = npc;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Npc getNpc() {
        return this.npc;
    }

    public Player getPlayer() {
        return this.player;
    }

}
