package uga.menik.cs4370.models;

/**
 * Represents a user in the system.
 */
public class Users {

    /**
     * Unique identifier for the user.
     */
    private final String userId;  // Changed from int to String

    /**
     * Username of the user.
     */
    private final String username;

    /**
     * Password of the user.
     */
    private final String password;

    /**
     * Constructs a User with specified details.
     *
     * @param userId    the unique identifier of the user as a String
     * @param username  the username of the user
     * @param password  the password of the user
     */
    public Users(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the user ID.
     *
     * @return the user ID as a String
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the username of the user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password of the user.
     *
     * This method should be used cautiously, considering security implications.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}