package com.runicrealms.runicnpcs.grid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Grid<E> {

    private GridBounds bounds;
    private Map<Integer, Set<E>> grid = new HashMap<Integer, Set<E>>();

    public Grid(GridBounds bounds) {
        this.bounds = bounds;
    }

    protected void insert(GridLocation location, E element) {
        int encoded = location.encodeToInt();
        if (!this.grid.containsKey(encoded)) {
            this.grid.put(encoded, new HashSet<E>());
        }
        this.grid.get(encoded).add(element);
    }

    protected Set<E> getSurroundingElements(GridLocation location) {
        Set<E> surrounding = new HashSet<E>();
        int encoded = location.encodeToInt();
        if (this.grid.containsKey(encoded)) {
            surrounding.addAll(this.grid.get(encoded));
        }
        for (Integer surrounder : location.getSurrounding()) {
            if (this.grid.containsKey(surrounder)) {
                surrounding.addAll(this.grid.get(surrounder));
            }
        }
        return surrounding;
    }

    public boolean containsElementInGrid(GridLocation location, E element) {
        int encoded = location.encodeToInt();
        if (!this.grid.containsKey(encoded)) {
            return false;
        }
        if (this.grid.get(encoded).contains(element)) {
            return true;
        }
        return false;
    }

    public void removeElementInGrid(GridLocation location, E element) {
        this.grid.get(location.encodeToInt()).remove(element);
    }

    public GridBounds getBounds() {
        return this.bounds;
    }

}
