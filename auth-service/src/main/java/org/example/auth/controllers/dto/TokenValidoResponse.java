package org.example.auth.controllers.dto;

public class TokenValidoResponse {

    private final long userId;
    private final String username;

    public TokenValidoResponse(long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
