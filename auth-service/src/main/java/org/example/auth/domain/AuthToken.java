package org.example.auth.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class AuthToken {

    private Long id;
    private String token;
    private Long userId;
    private TokenTipo tipo;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public AuthToken() {
    }

    public AuthToken(Long id, String token, Long userId, TokenTipo tipo,
                     LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.tipo = tipo;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TokenTipo getTipo() {
        return tipo;
    }

    public void setTipo(TokenTipo tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean estaExpirado(LocalDateTime ahora) {
        return expiresAt.isBefore(ahora);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthToken authToken)) {
            return false;
        }
        return id != null && id.equals(authToken.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
