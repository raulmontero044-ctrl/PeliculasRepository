package org.example.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MigracionesFlywayTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void lasTablasDeAutenticacionSeCrean() {
        Integer usuarios = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
        Integer tokens = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auth_tokens", Integer.class);

        assertTrue(usuarios != null && usuarios >= 0, "la tabla usuarios debe existir");
        assertTrue(tokens != null, "la tabla auth_tokens debe existir");
    }

    @Test
    void losUsuariosSemillaExistenYEstanHabilitados() {
        assertEquals("admin", jdbcTemplate.queryForObject(
                "SELECT username FROM usuarios WHERE id = 1", String.class));
        assertEquals("editor", jdbcTemplate.queryForObject(
                "SELECT username FROM usuarios WHERE id = 2", String.class));
        assertEquals("viewer", jdbcTemplate.queryForObject(
                "SELECT username FROM usuarios WHERE id = 3", String.class));

        assertEquals(Integer.valueOf(3), jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE username IN ('admin', 'editor', 'viewer') "
                        + "AND enabled = 1", Integer.class));
    }

    @Test
    void laTablaDeTokensNoTieneDatosSemilla() {
        assertEquals(Integer.valueOf(0),
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auth_tokens", Integer.class));
    }

    @Test
    void losHashesDeLosUsuariosSemillaSonBcrypt() {
        String hash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM usuarios WHERE username = ?", String.class, "admin");

        assertTrue(hash.startsWith("$2a$"), "el password debe estar hasheado con BCrypt");
    }

    @Test
    void elUsernameDeUsuarioEsUnico() {
        String sql = "INSERT INTO usuarios (username, password_hash, enabled) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "duplicado", "hash", true);

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(sql, "duplicado", "hash", true));
    }

    @Test
    void elTipoDeTokenSoloAdmiteAccessORefresh() {
        jdbcTemplate.update("INSERT INTO usuarios (username, password_hash, enabled) VALUES (?, ?, ?)",
                "usuario-tokens", "hash", true);
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM usuarios WHERE username = ?", Long.class, "usuario-tokens");

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
                "INSERT INTO auth_tokens (token, user_id, tipo, expires_at) VALUES (?, ?, ?, ?)",
                "token-falso", userId, "INVALIDO",
                java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().plusDays(1))));
    }
}
