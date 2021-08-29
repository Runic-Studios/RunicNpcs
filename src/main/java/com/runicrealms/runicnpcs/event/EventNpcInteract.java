package com.runicrealms.runicnpcs.event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.Plugin;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                    int entityID = packet.getIntegers().read(0);
                    try {
                        Npc npc = Plugin.getNpcEntities().get(entityID);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getInstance(),
                                () -> Bukkit.getServer().getPluginManager().callEvent(new NpcClickEvent(npc, event.getPlayer())));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Bukkit.getLogger().info(ChatColor.DARK_RED + "An npc could not be found by entity Id!");
                    }
                }
            }
        });
    }

}
