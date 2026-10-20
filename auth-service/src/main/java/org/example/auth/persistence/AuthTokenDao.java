package org.example.auth.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.auth.domain.AuthToken;
import org.example.auth.domain.TokenTipo;
import org.example.auth.domain.repository.AuthTokenRepository;
import org.example.auth.persistence.rowmapper.AuthTokenRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthTokenDao implements AuthTokenRepository {

    private static final String COLUMNAS = "id, token, user_id, tipo, expires_at, created_at";

    private final JdbcTemplate jdbcTemplate;
    private final AuthTokenRowMapper authTokenRowMapper;

    public AuthTokenDao(JdbcTemplate jdbcTemplate, AuthTokenRowMapper authTokenRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.authTokenRowMapper = authTokenRowMapper;
    }

    @Override
    public AuthToken save(AuthToken token) {
        if (token.getId() == null) {
            return insert(token);
        }
        jdbcTemplate.update(
                "UPDATE auth_tokens SET token = ?, user_id = ?, tipo = ?, expires_at = ?, "
                        + "created_at = ? WHERE id = ?",
                token.getToken(),
                token.getUserId(),
                token.getTipo().name(),
                token.getExpiresAt(),
                token.getCreatedAt(),
                token.getId());
        return token;
    }

    @Override
    public Optional<AuthToken> findByToken(String token) {
        List<AuthToken> resultado = jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM auth_tokens WHERE token = ?",
                authTokenRowMapper, token);
        return resultado.stream().findFirst();
    }

    @Override
    public Optional<AuthToken> findValidByToken(String token, TokenTipo tipo, LocalDateTime now) {
        List<AuthToken> resultado = jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM auth_tokens WHERE token = ? AND tipo = ? "
                        + "AND expires_at >= ?",
                authTokenRowMapper, token, tipo.name(), now);
        return resultado.stream().findFirst();
    }

    private AuthToken insert(AuthToken token) {
        jdbcTemplate.update(
                "INSERT INTO auth_tokens (token, user_id, tipo, expires_at, created_at) "
                        + "VALUES (?, ?, ?, ?, ?)",
                token.getToken(),
                token.getUserId(),
                token.getTipo().name(),
                java.sql.Timestamp.valueOf(token.getExpiresAt()),
                java.sql.Timestamp.valueOf(token.getCreatedAt()));
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM auth_tokens WHERE token = ?", Long.class, token.getToken());
        token.setId(id);
        return token;
    }
}
