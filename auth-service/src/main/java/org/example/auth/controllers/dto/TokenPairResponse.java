package org.example.auth.controllers.dto;

public class TokenPairResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresInAccess;
    private final long expiresInRefresh;

    public TokenPairResponse(String accessToken, String refreshToken, String tokenType,
                             long expiresInAccess, long expiresInRefresh) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresInAccess = expiresInAccess;
        this.expiresInRefresh = expiresInRefresh;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInAccess() {
        return expiresInAccess;
    }

    public long getExpiresInRefresh() {
        return expiresInRefresh;
    }
}
