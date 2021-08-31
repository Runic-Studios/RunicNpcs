package com.runicrealms.runicnpcs.event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.Plugin;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * Handles the creation of our custom NpcClickEvent
 */
public class EventNpcInteract implements Listener {

    public EventNpcInteract() {
        Plugin.getProtocolManager().addPacketListener(new PacketAdapter(Plugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                    PacketContainer packet = event.getPacket();
                    if (packet.getEntityUseActions().readSafely(0) == EnumWrappers.EntityUseAction.ATTACK) return;
                    if (packet.getEntityUseActions().readSafely(0) == EnumWrappers.EntityUseAction.INTERACT_AT) return;
                    if (packet.getHands().readSafely(0) == EnumWrappers.Hand.OFF_HAND) return;
                    int entityID = packet.getIntegers().read(0);
                    Npc npc = Plugin.getNpcEntities().get(entityID);
                    if (npc == null) return;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getInstance(),
                            () -> Bukkit.getServer().getPluginManager().callEvent(new NpcClickEvent(npc, event.getPlayer())));
                }
            }
        });
    }

}
