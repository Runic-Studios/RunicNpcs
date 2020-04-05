package com.runicrealms.runicnpcs.api;

import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.Plugin;
import net.minecraft.server.v1_15_R1.EntityPlayer;

public class RunicNpcsAPI {

    public static boolean isNpc(EntityPlayer player) {
        return Plugin.getNpcEntityIds().containsKey(player.getId());
    }

    public static Npc getNpcFromEntityId(Integer entityId) {
        return Plugin.getNpcEntityIds().get(entityId);
    }

    public static Npc getNpcById(Integer id) {
        return Plugin.getNpcs().get(id);
    }

}
