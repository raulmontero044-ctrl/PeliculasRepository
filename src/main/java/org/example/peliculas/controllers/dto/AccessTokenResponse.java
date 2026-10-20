package org.example.peliculas.controllers.dto;

public class AccessTokenResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresInAccess;

    public AccessTokenResponse(String accessToken, String tokenType, long expiresInAccess) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInAccess = expiresInAccess;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInAccess() {
        return expiresInAccess;
    }
}
