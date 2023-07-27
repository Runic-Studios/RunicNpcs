package com.runicrealms.plugin.npcs.event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.runicrealms.plugin.npcs.Npc;
import com.runicrealms.plugin.npcs.RunicNpcs;
import com.runicrealms.plugin.npcs.api.NpcClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * Handles the creation of our custom NpcClickEvent
 * <p>
 * Visit <a href="https://ci.dmulloy2.net/job/ProtocolLib/javadoc/com/comphenix/protocol/PacketType.html">ProtocolLib Docs</a>
 */
public class EventNpcInteract implements Listener {
    private static AsyncListenerHandler asyncListener;

    public EventNpcInteract() {
        asyncListener = RunicNpcs.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(RunicNpcs.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                    PacketContainer packet = event.getPacket();
                    WrappedEnumEntityUseAction useAction = packet.getEnumEntityUseActions().readSafely(0);
                    EnumWrappers.EntityUseAction action = useAction.getAction();

                    if(action == EnumWrappers.EntityUseAction.ATTACK) return;
                    if(action == EnumWrappers.EntityUseAction.INTERACT) return;
                    if(action == EnumWrappers.EntityUseAction.INTERACT_AT) {
                            if(useAction.getHand() == EnumWrappers.Hand.OFF_HAND) return;
                            int entityID = packet.getIntegers().read(0);
                            Npc npc = RunicNpcs.getNpcEntities().get(entityID);
                            if (npc == null) return;
                            Bukkit.getScheduler().scheduleSyncDelayedTask(RunicNpcs.getInstance(),
                                () -> {
                                    Bukkit.getServer().getPluginManager().callEvent(new NpcClickEvent(npc, event.getPlayer()));
                            });
                    }
                }
            }
        });

        asyncListener.start();
    }

    public static void stopTask() {
        asyncListener.stop();
    }
}
