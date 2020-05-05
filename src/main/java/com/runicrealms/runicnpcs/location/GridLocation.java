package com.runicrealms.runicnpcs.location;

import java.util.ArrayList;
import java.util.List;

public class GridLocation {

    private short x;
    private short y;
    private Grid grid;

    public GridLocation(Grid grid, short x, short y) {
        this.grid = grid;
        this.x = x;
        this.y = y;
    }

    public Grid getGrid() {
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

    public static GridLocation decodeFromInt(Grid grid, int encoded) {
        return new GridLocation(grid, (short) (encoded >> 16), (short) (encoded & 0xFFFF));
    }

    public List<Integer> getSurrounding() {
        List<Integer> surrounding = new ArrayList<Integer>(GridDirection.values().length);
        for (GridDirection direction : GridDirection.values()) {
            if (this.x + direction.getX() >= this.grid.getBounds().getX1() &&
                    this.x + direction.getX() <= this.grid.getBounds().getX2() &&
                    this.y + direction.getY() >= this.grid.getBounds().getY1() &&
                    this.y + direction.getY() <= this.grid.getBounds().getY2()) {
                surrounding.add(new GridLocation(this.grid, (short) (this.x + direction.getX()), (short) (this.y + direction.getY())).encodeToInt());
            }
        }
        return surrounding;
    }

}