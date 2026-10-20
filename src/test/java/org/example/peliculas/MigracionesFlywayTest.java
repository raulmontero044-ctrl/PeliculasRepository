package org.example.peliculas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MigracionesFlywayTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void lasTablasSeCreanYSeCarganDatos() {
        Integer peliculas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM peliculas", Integer.class);
        Integer actores = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM actores", Integer.class);

        assertTrue(peliculas != null && peliculas > 0, "debe haber películas de ejemplo");
        assertTrue(actores != null && actores > 0, "debe haber actores de ejemplo");
    }

    @Test
    void laTablaIntermediaTieneRelaciones() {
        Integer asignaciones = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pelicula_actor", Integer.class);

        assertTrue(asignaciones != null && asignaciones > 0, "debe haber asignaciones de ejemplo");
    }

    @Test
    void elTituloYAnioSonUnicos() {
        Map<String, Object> pelicula = jdbcTemplate.queryForMap("SELECT titulo, anio FROM peliculas LIMIT 1");

        assertThrows(DataAccessException.class, () -> jdbcTemplate.update(
                "INSERT INTO peliculas (titulo, anio, director, genero, duracion_minutos) VALUES (?, ?, ?, ?, ?)",
                pelicula.get("titulo"), pelicula.get("anio"), "Otro", "DRAMA", 100));
    }

    @Test
    void noSePuedeBorrarUnActorConPeliculas() {
        Map<String, Object> actor = jdbcTemplate.queryForMap(
                "SELECT actor_id FROM pelicula_actor LIMIT 1");

        assertThrows(DataAccessException.class,
                () -> jdbcTemplate.update("DELETE FROM actores WHERE id = ?", actor.get("actor_id")));
    }

    @Test
    void sePuedeListarConJoin() {
        Integer asignaciones = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pelicula_actor", Integer.class);
        Integer conJoin = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM peliculas p JOIN pelicula_actor pa ON pa.pelicula_id = p.id",
                Integer.class);

        assertEquals(asignaciones, conJoin);
    }

    @Test
    void lasTablasDeAutenticacionSeCreanVacias() {
        assertEquals(Integer.valueOf(0),
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class));
        assertEquals(Integer.valueOf(0),
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auth_tokens", Integer.class));
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
                "admin", "hash", true);
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM usuarios WHERE username = ?", Long.class, "admin");

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
                "INSERT INTO auth_tokens (token, user_id, tipo, expires_at) VALUES (?, ?, ?, ?)",
                "token-falso", userId, "INVALIDO",
                java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().plusDays(1))));
    }
}
