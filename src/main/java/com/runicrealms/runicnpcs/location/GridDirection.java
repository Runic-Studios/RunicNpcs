package com.runicrealms.runicnpcs.location;

public enum GridDirection {

    N((byte) 0, (byte) 1),
    NE((byte) 1, (byte) 1),
    E((byte) 1, (byte) 0),
    SE((byte) 1, (byte) -1),
    S((byte) 0, (byte) -1),
    SW((byte) -1, (byte) -1),
    W((byte) -1, (byte) 0),
    NW((byte) -1, (byte) 1);

    private byte x;
    private byte y;

    GridDirection(byte x, byte y) {
        this.x = x;
        this.y = y;
    }

    public byte getX() {
        return this.x;
    }

    public byte getY() {
        return this.y;
    }

}
