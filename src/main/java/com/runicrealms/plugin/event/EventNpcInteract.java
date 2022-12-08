package com.runicrealms.plugin.event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.runicrealms.plugin.Npc;
import com.runicrealms.plugin.RunicNpcs;
import com.runicrealms.plugin.api.NpcClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * Handles the creation of our custom NpcClickEvent
 */
public class EventNpcInteract implements Listener {

    public EventNpcInteract() {
        RunicNpcs.getProtocolManager().addPacketListener(new PacketAdapter(RunicNpcs.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                    PacketContainer packet = event.getPacket();
                    if (packet.getEntityUseActions().readSafely(0) == EnumWrappers.EntityUseAction.ATTACK) return;
                    if (packet.getEntityUseActions().readSafely(0) == EnumWrappers.EntityUseAction.INTERACT_AT) return;
                    if (packet.getHands().readSafely(0) == EnumWrappers.Hand.OFF_HAND) return;
                    int entityID = packet.getIntegers().read(0);
                    Npc npc = RunicNpcs.getNpcEntities().get(entityID);
                    if (npc == null) return;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(RunicNpcs.getInstance(),
                            () -> Bukkit.getServer().getPluginManager().callEvent(new NpcClickEvent(npc, event.getPlayer())));
                }
            }
        });
    }

}
