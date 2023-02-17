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
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the creation of our custom NpcClickEvent
 * <p>
 * Visit <a href="https://ci.dmulloy2.net/job/ProtocolLib/javadoc/com/comphenix/protocol/PacketType.html">ProtocolLib Docs</a>
 */
public class EventNpcInteract implements Listener {
    private final Set<UUID> npcTalkers = new HashSet<>();

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
                    if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
                        npcTalkers.add(event.getPlayer().getUniqueId());
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(RunicNpcs.getInstance(),
                            () -> Bukkit.getServer().getPluginManager().callEvent(new NpcClickEvent(npc, event.getPlayer())));
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (npcTalkers.contains(event.getPlayer().getUniqueId())) {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            npcTalkers.remove(event.getPlayer().getUniqueId());
        }
    }

}
