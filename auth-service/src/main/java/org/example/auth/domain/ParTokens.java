package org.example.auth.domain;

public record ParTokens(AuthToken accessToken, AuthToken refreshToken) {
}
