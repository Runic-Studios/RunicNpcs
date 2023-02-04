package com.runicrealms.plugin;

import org.bukkit.ChatColor;

public enum NpcTag {
    BANKER(ChatColor.GRAY, "Banker"),
    MERCHANT(ChatColor.GREEN, "Merchant"),
    NPC(ChatColor.GRAY, "NPC"),
    QUEST(ChatColor.GOLD, "Quest");

    private final ChatColor chatColor;
    private final String identifier;

    NpcTag(ChatColor chatColor, String identifier) {
        this.chatColor = chatColor;
        this.identifier = identifier;
    }

    public static NpcTag getFromIdentifier(String identifier) {
        for (NpcTag npcTag : NpcTag.values()) {
            if (npcTag.getIdentifier().equalsIgnoreCase(identifier))
                return npcTag;
        }
        return null;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getIdentifier() {
        return identifier;
    }
}
