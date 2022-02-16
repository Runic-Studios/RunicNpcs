package com.runicrealms.runicnpcs.api;

import com.runicrealms.runicnpcs.Npc;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NpcClickEvent extends Event {

    private final Npc npc;
    private final Player player;

    private static final HandlerList handlers = new HandlerList();

    public NpcClickEvent(Npc npc, Player player) {
        this.npc = npc;
        this.player = player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Npc getNpc() {
        return this.npc;
    }

    public Player getPlayer() {
        return this.player;
    }

}
