package uga.menik.cs4370.models;

/**
 * Represents a dining hall in the system.
 */
public class DiningHall {

    /**
     * Unique identifier for the dining hall.
     */
    private final int diningHallId;

    /**
     * Name of the dining hall.
     */
    private final String diningHallName;

    /**
     * Constructs a DiningHall with specified details.
     *
     * @param diningHallId   the unique identifier of the dining hall
     * @param diningHallName the name of the dining hall
     */
    public DiningHall(int diningHallId, String diningHallName) {
        this.diningHallId = diningHallId;
        this.diningHallName = diningHallName;
    }

    /**
     * Returns the dining hall ID.
     *
     * @return the dining hall ID
     */
    public int getDiningHallId() {
        return diningHallId;
    }

    /**
     * Returns the name of the dining hall.
     *
     * @return the dining hall name
     */
    public String getDiningHallName() {
        return diningHallName;
    }
}