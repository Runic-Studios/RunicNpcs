package com.runicrealms.runicnpcs.event;

import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.Plugin;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

public class EventNpcInteract implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            if (Plugin.getNpcEntityIds().containsKey(((CraftPlayer) event.getEntity()).getHandle().getId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        if (event.getHand().equals(EquipmentSlot.HAND)) {
            if (event.getRightClicked().getType() == EntityType.PLAYER) {
                if (Plugin.getNpcEntityIds().containsKey(((CraftPlayer) event.getRightClicked()).getHandle().getId())) {
                    Bukkit.getServer().getPluginManager().callEvent(new NpcClickEvent(Plugin.getNpcEntityIds().get(((CraftPlayer) event.getRightClicked()).getHandle().getId()), event.getPlayer()));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Map.Entry<Integer, Npc> entry : Plugin.getNpcs().entrySet()) {
            entry.getValue().spawnForPlayer(event.getPlayer());
        }
    }

}
