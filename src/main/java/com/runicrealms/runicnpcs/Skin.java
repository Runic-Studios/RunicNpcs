package com.runicrealms.runicnpcs;

public class Skin {

    private String texture;
    private String signature;

    public Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public String getTexture() {
        return this.texture;
    }

    public String getSignature() {
        return this.signature;
    }

}
