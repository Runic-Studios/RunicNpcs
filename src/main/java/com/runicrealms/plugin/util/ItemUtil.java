package com.runicrealms.plugin.util;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static boolean isEquippable(EnumWrappers.ItemSlot itemSlot, ItemStack itemStack) {
        EquipmentSlot equipmentSlot;

        switch(itemSlot) {
            case MAINHAND -> equipmentSlot = EquipmentSlot.HAND;
            case OFFHAND -> equipmentSlot = EquipmentSlot.OFF_HAND;
            default -> equipmentSlot = EquipmentSlot.valueOf(itemSlot.name().toUpperCase());
        }

        return isEquippable(equipmentSlot, itemStack);
    }

    public static boolean isEquippable(EquipmentSlot slot, ItemStack itemStack) {
        if(itemStack == null) return false;
        String name = itemStack.getType().name().toLowerCase();
        switch(slot) {
            case CHEST -> {
                return name.endsWith("chestplate") || name.equals("elytra");
            }
            case LEGS -> {
                return name.endsWith("leggings") || name.endsWith("pants");
            }
            case FEET -> {
                return name.endsWith("boots");
            }
            default -> { return true; }
        }
    }
}
