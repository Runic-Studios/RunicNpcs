package com.runicrealms.plugin.npcs;

public class Skin {

    private final String texture;
    private final String signature;

    public Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getTexture() {
        return this.texture;
    }

}
