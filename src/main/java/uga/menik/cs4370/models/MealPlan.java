package uga.menik.cs4370.models;

/**
 * Represents a meal plan in the system.
 */
public class MealPlan {

    /**
     * Unique identifier for the meal plan.
     */
    private final int mealPlanId;

    /**
     * Name of the meal plan.
     */
    private final String mealPlanName;

    /**
     * User ID associated with this meal plan.
     */
    private final String userId;  // Changed from int to String

    /**
     * Dining Hall ID associated with this meal plan.
     */
    private final int diningHallId;

    /**
     * Constructs a MealPlan with specified details.
     *
     * @param mealPlanId     the unique identifier of the meal plan
     * @param mealPlanName   the name of the meal plan
     * @param userId         the user ID (as a String) associated with the meal plan
     * @param diningHallId   the dining hall ID associated with the meal plan
     */
    public MealPlan(int mealPlanId, String mealPlanName, String userId, int diningHallId) {
        this.mealPlanId = mealPlanId;
        this.mealPlanName = mealPlanName;
        this.userId = userId;
        this.diningHallId = diningHallId;
    }

    /**
     * Returns the meal plan ID.
     *
     * @return the meal plan ID
     */
    public int getMealPlanId() {
        return mealPlanId;
    }

    /**
     * Returns the name of the meal plan.
     *
     * @return the meal plan name
     */
    public String getMealPlanName() {
        return mealPlanName;
    }

    /**
     * Returns the user ID associated with this meal plan.
     *
     * @return the user ID as a String
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the dining hall ID associated with this meal plan.
     *
     * @return the dining hall ID
     */
    public int getDiningHallId() {
        return diningHallId;
    }
}