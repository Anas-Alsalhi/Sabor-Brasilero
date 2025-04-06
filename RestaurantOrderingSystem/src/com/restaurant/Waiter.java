package com.restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a waiter in the restaurant.
 */
public final class Waiter extends Staff {

    private int waiterId;
    private List<Table> assignedTables;

    /**
     * Default constructor for deserialization.
     */
    public Waiter() {
        super();
        this.waiterId = 0;
        this.assignedTables = new ArrayList<>();
    }

    /**
     * Constructor for creating a Waiter.
     *
     * @param name     The name of the waiter.
     * @param waiterId The unique ID of the waiter.
     */
    public Waiter(String name, int waiterId) {
        super(name);
        this.waiterId = waiterId;
        this.assignedTables = new ArrayList<>();
    }

    public int getWaiterId() {
        return waiterId;
    }

    public void assignTable(Table table) {
        assignedTables.add(table);
    }

    public List<Table> getAssignedTables() {
        return Collections.unmodifiableList(assignedTables);
    }

    /**
     * Implementation of duties performed by the waiter.
     */
    @Override
    public void performDuties() {
        System.out.println(getName() + " (ID: " + waiterId + ") is serving customers and ensuring orders are delivered promptly.");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Waiter waiter = (Waiter) obj;
        return waiterId == waiter.waiterId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(waiterId);
    }
}
