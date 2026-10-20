package org.example.peliculas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;
import org.example.peliculas.domain.AuthToken;
import org.example.peliculas.domain.TokenTipo;
import org.example.peliculas.domain.Usuario;
import org.example.peliculas.domain.repository.AuthTokenRepository;
import org.example.peliculas.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthTokenDaoTest {

    private static final LocalDateTime AHORA = LocalDateTime.parse("2026-01-15T10:00:00");

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = usuarioRepository.save(new Usuario(null, "token-user", "hash", true)).getId();
    }

    private AuthToken nuevo(TokenTipo tipo, LocalDateTime expiresAt) {
        return new AuthToken(null, UUID.randomUUID().toString(), userId, tipo, expiresAt, AHORA);
    }

    @Test
    void save_insertaYDevuelveElTokenPorSuValor() {
        AuthToken guardado = authTokenRepository.save(nuevo(TokenTipo.ACCESS, AHORA.plusMinutes(5)));

        assertTrue(guardado.getId() != null);
        AuthToken recargado = authTokenRepository.findByToken(guardado.getToken()).orElseThrow();
        assertEquals(TokenTipo.ACCESS, recargado.getTipo());
        assertEquals(userId, recargado.getUserId());
        assertEquals(AHORA, recargado.getCreatedAt());
        assertEquals(AHORA.plusMinutes(5), recargado.getExpiresAt());
    }

    @Test
    void save_actualizaElTokenExistente() {
        AuthToken guardado = authTokenRepository.save(nuevo(TokenTipo.ACCESS, AHORA.plusMinutes(5)));
        guardado.setExpiresAt(AHORA.plusMinutes(10));

        authTokenRepository.save(guardado);

        AuthToken recargado = authTokenRepository.findByToken(guardado.getToken()).orElseThrow();
        assertEquals(AHORA.plusMinutes(10), recargado.getExpiresAt());
    }

    @Test
    void findByToken_inexistente_devuelveVacio() {
        assertTrue(authTokenRepository.findByToken("no-existe").isEmpty());
    }

    @Test
    void findValidByToken_devuelveElTokenSiEsDelTipoYNoHaExpirado() {
        AuthToken access = authTokenRepository.save(nuevo(TokenTipo.ACCESS, AHORA.plusMinutes(5)));
        authTokenRepository.save(nuevo(TokenTipo.REFRESH, AHORA.plusDays(1)));

        assertTrue(authTokenRepository
                .findValidByToken(access.getToken(), TokenTipo.ACCESS, AHORA).isPresent());
    }

    @Test
    void findValidByToken_expirado_devuelveVacio() {
        AuthToken caducado = authTokenRepository.save(nuevo(TokenTipo.ACCESS, AHORA.minusMinutes(1)));

        assertTrue(authTokenRepository
                .findValidByToken(caducado.getToken(), TokenTipo.ACCESS, AHORA).isEmpty());
    }

    @Test
    void findValidByToken_tipoDistinto_devuelveVacio() {
        AuthToken refresh = authTokenRepository.save(nuevo(TokenTipo.REFRESH, AHORA.plusDays(1)));

        assertTrue(authTokenRepository
                .findValidByToken(refresh.getToken(), TokenTipo.ACCESS, AHORA).isEmpty());
    }

    @Test
    void elTokenSeAlmacenaComoUnico() {
        AuthToken original = authTokenRepository.save(nuevo(TokenTipo.ACCESS, AHORA.plusMinutes(5)));
        AuthToken duplicado = nuevo(TokenTipo.REFRESH, AHORA.plusDays(1));
        duplicado.setToken(original.getToken());

        assertThrows(DataIntegrityViolationException.class,
                () -> authTokenRepository.save(duplicado));
    }
}
