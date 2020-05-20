package com.runicrealms.runicnpcs.grid;

import java.util.ArrayList;
import java.util.List;

public class GridLocation {

    private short x;
    private short y;
    @SuppressWarnings("rawtypes")
    private Grid grid;

    public GridLocation(@SuppressWarnings("rawtypes") Grid grid, short x, short y) {
        this.grid = grid;
        this.x = x;
        this.y = y;
    }

    public @SuppressWarnings("rawtypes") Grid getGrid() {
        return this.grid;
    }

    public short getX() {
        return this.x;
    }

    public short getY() {
        return this.y;
    }

    public int encodeToInt() {
        return (this.x << 16) | (this.y & 0xFFFF);
    }

    public static GridLocation decodeFromInt(@SuppressWarnings("rawtypes") Grid grid, int encoded) {
        return new GridLocation(grid, (short) (encoded >> 16), (short) (encoded & 0xFFFF));
    }

    public List<Integer> getSurrounding() {
        List<Integer> surrounding = new ArrayList<Integer>(GridDirection.values().length);
        for (GridDirection direction : GridDirection.values()) {
            if (this.grid.getBounds().isInBounds(this.x + direction.getX(), this.y + direction.getY())) {
                surrounding.add(new GridLocation(this.grid, (short) (this.x + direction.getX()), (short) (this.y + direction.getY())).encodeToInt());
            }
        }
        return surrounding;
    }

}