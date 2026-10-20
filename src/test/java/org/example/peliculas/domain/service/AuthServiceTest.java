package org.example.peliculas.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.example.peliculas.domain.AuthToken;
import org.example.peliculas.domain.ParTokens;
import org.example.peliculas.domain.TokenTipo;
import org.example.peliculas.domain.Usuario;
import org.example.peliculas.domain.exceptions.CredencialesIncorrectasException;
import org.example.peliculas.domain.exceptions.TokenExpiradoException;
import org.example.peliculas.domain.exceptions.TokenInvalidoException;
import org.example.peliculas.domain.exceptions.UsuarioNoEncontradoException;
import org.example.peliculas.domain.exceptions.UsuarioYaExisteException;
import org.example.peliculas.domain.fake.InMemoryAuthTokenRepository;
import org.example.peliculas.domain.fake.InMemoryUsuarioRepository;
import org.example.peliculas.domain.fake.PasswordHasherFalso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private static final Instant MOMENTO = Instant.parse("2026-01-15T10:00:00Z");

    private InMemoryUsuarioRepository usuarios;
    private InMemoryAuthTokenRepository tokens;
    private PasswordHasherFalso hasher;
    private AuthService service;

    @BeforeEach
    void setUp() {
        usuarios = new InMemoryUsuarioRepository();
        tokens = new InMemoryAuthTokenRepository();
        hasher = new PasswordHasherFalso();
        service = nuevoServicio(MOMENTO);
    }

    private AuthService nuevoServicio(Instant instante) {
        return new AuthService(usuarios, tokens, hasher,
                Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void register_creaElUsuarioSinGenerarTokens() {
        Usuario creado = service.register("raul", "secret123");

        assertNotNull(creado.getId());
        assertEquals("raul", creado.getUsername());
        assertEquals("hash:secret123", creado.getPasswordHash());
        assertTrue(creado.isEnabled());
        assertTrue(tokens.findAll().isEmpty(), "el registro no debe generar tokens");
    }

    @Test
    void register_usernameYaExistente_lanzaUsuarioYaExiste() {
        service.register("raul", "secret123");

        UsuarioYaExisteException excepcion = assertThrows(UsuarioYaExisteException.class,
                () -> service.register("raul", "otra1234"));

        assertTrue(excepcion.getMessage().contains("raul"));
        assertEquals(1, usuarios.findAll().size());
    }

    @Test
    void login_usuarioNoExiste_lanzaUsuarioNoEncontradoConMensajePropio() {
        UsuarioNoEncontradoException excepcion = assertThrows(UsuarioNoEncontradoException.class,
                () -> service.login("fantasma", "secret123"));

        assertEquals("Usuario no existe", excepcion.getMessage());
    }

    @Test
    void login_passwordIncorrecta_lanzaCredencialesIncorrectasConMensajePropio() {
        service.register("raul", "secret123");

        CredencialesIncorrectasException excepcion = assertThrows(
                CredencialesIncorrectasException.class,
                () -> service.login("raul", "otra1234"));

        assertEquals("Credenciales incorrectas", excepcion.getMessage());
    }

    @Test
    void login_valido_devuelveParDeTokensConSusTiemposDeVida() {
        service.register("raul", "secret123");

        ParTokens par = service.login("raul", "secret123");

        assertEquals(TokenTipo.ACCESS, par.accessToken().getTipo());
        assertEquals(TokenTipo.REFRESH, par.refreshToken().getTipo());
        assertEquals(LocalDateTime.parse("2026-01-15T10:05:00"), par.accessToken().getExpiresAt());
        assertEquals(LocalDateTime.parse("2026-01-16T10:00:00"), par.refreshToken().getExpiresAt());
        assertEquals(2, tokens.findAll().size());
    }

    @Test
    void login_generaTokensDistintosEnCadaSesion() {
        service.register("raul", "secret123");

        ParTokens primera = service.login("raul", "secret123");
        ParTokens segunda = service.login("raul", "secret123");

        assertNotEquals(primera.accessToken().getToken(), segunda.accessToken().getToken());
        assertNotEquals(primera.refreshToken().getToken(), segunda.refreshToken().getToken());
    }

    @Test
    void refresh_devuelveSoloUnNuevoAccessTokenSinRotarElRefresh() {
        service.register("raul", "secret123");
        ParTokens par = service.login("raul", "secret123");
        int tokensAntes = tokens.findAll().size();

        AuthToken nuevoAccess = service.refresh(par.refreshToken().getToken());

        assertEquals(TokenTipo.ACCESS, nuevoAccess.getTipo());
        assertEquals(LocalDateTime.parse("2026-01-15T10:05:00"), nuevoAccess.getExpiresAt());
        assertEquals(tokensAntes + 1, tokens.findAll().size());
        assertTrue(tokens.findByToken(par.refreshToken().getToken()).isPresent(),
                "el refreshToken no debe rotarse ni eliminarse");
    }

    @Test
    void refresh_tokenInexistente_lanzaTokenInvalido() {
        assertThrows(TokenInvalidoException.class, () -> service.refresh("no-existe"));
    }

    @Test
    void refresh_conUnAccessToken_lanzaTokenInvalido() {
        service.register("raul", "secret123");
        ParTokens par = service.login("raul", "secret123");

        assertThrows(TokenInvalidoException.class,
                () -> service.refresh(par.accessToken().getToken()));
    }

    @Test
    void refresh_tokenExpirado_lanzaTokenExpirado() {
        service.register("raul", "secret123");
        ParTokens par = service.login("raul", "secret123");
        AuthService dosDiasDespues = nuevoServicio(MOMENTO.plusSeconds(2 * 24 * 60 * 60));

        assertThrows(TokenExpiradoException.class,
                () -> dosDiasDespues.refresh(par.refreshToken().getToken()));
    }

    @Test
    void validateAccessToken_valido_devuelveElUsuario() {
        service.register("raul", "secret123");
        ParTokens par = service.login("raul", "secret123");

        Usuario usuario = service.validateAccessToken(par.accessToken().getToken());

        assertEquals("raul", usuario.getUsername());
    }

    @Test
    void validateAccessToken_expirado_lanzaTokenExpirado() {
        service.register("raul", "secret123");
        ParTokens par = service.login("raul", "secret123");
        AuthService seisMinutosDespues = nuevoServicio(MOMENTO.plusSeconds(6 * 60));

        assertThrows(TokenExpiradoException.class,
                () -> seisMinutosDespues.validateAccessToken(par.accessToken().getToken()));
    }

    @Test
    void validateAccessToken_unRefreshTokenNoSirve() {
        service.register("raul", "secret123");
        ParTokens par = service.login("raul", "secret123");

        assertThrows(TokenInvalidoException.class,
                () -> service.validateAccessToken(par.refreshToken().getToken()));
    }

    @Test
    void validateAccessToken_tokenInexistente_lanzaTokenInvalido() {
        assertThrows(TokenInvalidoException.class, () -> service.validateAccessToken("otro"));
    }

    @Test
    void validateAccessToken_soloSeInvalidaPorExpiracion() {
        service.register("raul", "secret123");
        ParTokens par = service.login("raul", "secret123");
        AuthToken antiguo = par.accessToken();
        antiguo.setCreatedAt(LocalDateTime.parse("2026-01-10T10:00:00"));
        tokens.save(antiguo);

        assertNotNull(service.validateAccessToken(antiguo.getToken()));
    }
}
