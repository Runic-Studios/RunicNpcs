package com.runicrealms.runicnpcs.grid;

import com.runicrealms.runicnpcs.Npc;
import org.bukkit.Location;

import java.util.Set;

public class NpcGrid extends Grid<Npc> {

    private short blocksPerBox;

    public NpcGrid(GridBounds bounds, short blocksPerBox) {
        super(bounds);
        this.blocksPerBox = blocksPerBox;
    }

    public Set<Npc> getNearbyNpcs(Location location) {
        return this.getSurroundingElements(this.getGridLocationFromLocation(location));
    }

    public GridLocation getGridLocationFromLocation(Location location) {
        return new GridLocation(this,
                (short) (location.getBlockX() / this.blocksPerBox),
                (short) (location.getBlockZ() / this.blocksPerBox));
    }

    public void insert(Npc npc) {
        this.insert(this.getGridLocationFromLocation(npc.getLocation()), npc);
    }

}
