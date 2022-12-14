package ch.disappointment.WalkoutCompanion.persistence.model;

/**
 * Model for representing an instance of the logged-in user's username and login token.
 */
public class User {
    private String username;
    private String token;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }
}
