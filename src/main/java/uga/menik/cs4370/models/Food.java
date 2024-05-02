package uga.menik.cs4370.models;

public class Food {
     /**
     * Unique identifier for the food.
     */
    private final int foodId;

    /**
     * Name of the food.
     */
    private final String foodName;

    /**
     * Food's calories.
     */
    private final int calories;


    /**
     * Constructs a DiningHall with specified details.
     *
     * @param diningHallId   the unique identifier of the dining hall
     * @param diningHallName the name of the dining hall
     */
    public Food(int foodId, String foodName, int calories) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.calories = calories;
    }

    /**
     * Returns the food ID.
     *
     * @return the food ID
     */
    public int getFoodId() {
        return foodId;
    }

    /**
     * Returns the name of the food.
     *
     * @return the food name
     */
    public String getFoodName() {
        return foodName;
    }

    /**
     * Returns the calories of the food.
     *
     * @return the food's calories
     */
    public int getCalories() {
        return calories;
    }
}
