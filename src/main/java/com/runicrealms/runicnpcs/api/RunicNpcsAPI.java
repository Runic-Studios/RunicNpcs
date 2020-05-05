package com.runicrealms.runicnpcs.api;

import com.runicrealms.runicnpcs.Npc;
import com.runicrealms.runicnpcs.Plugin;
import net.minecraft.server.v1_15_R1.EntityPlayer;

public class RunicNpcsAPI {

    public static boolean isNpc(EntityPlayer player) {
        return Plugin.getNpcEntities().containsKey(player);
    }

    public static Npc getNpcById(Integer id) {
        return Plugin.getNpcs().get(id);
    }

}
