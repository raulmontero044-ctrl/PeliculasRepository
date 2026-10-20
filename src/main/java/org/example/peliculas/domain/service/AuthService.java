package org.example.peliculas.domain.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.example.peliculas.domain.AuthToken;
import org.example.peliculas.domain.ParTokens;
import org.example.peliculas.domain.TokenTipo;
import org.example.peliculas.domain.Usuario;
import org.example.peliculas.domain.exceptions.CredencialesIncorrectasException;
import org.example.peliculas.domain.exceptions.TokenExpiradoException;
import org.example.peliculas.domain.exceptions.TokenInvalidoException;
import org.example.peliculas.domain.exceptions.UsuarioNoEncontradoException;
import org.example.peliculas.domain.exceptions.UsuarioYaExisteException;
import org.example.peliculas.domain.repository.AuthTokenRepository;
import org.example.peliculas.domain.repository.PasswordHasher;
import org.example.peliculas.domain.repository.UsuarioRepository;

public class AuthService {

    public static final Duration DURACION_ACCESS_TOKEN = Duration.ofMinutes(5);
    public static final Duration DURACION_REFRESH_TOKEN = Duration.ofDays(1);

    private static final int LONGITUD_TOKEN = 32;

    private final UsuarioRepository usuarioRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UsuarioRepository usuarioRepository,
                       AuthTokenRepository authTokenRepository,
                       PasswordHasher passwordHasher,
                       Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    public Usuario register(String username, String password) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new UsuarioYaExisteException(username);
        }
        Usuario usuario = new Usuario(null, username, passwordHasher.hash(password), true);
        return usuarioRepository.save(usuario);
    }

    public ParTokens login(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(UsuarioNoEncontradoException::new);
        if (!passwordHasher.matches(password, usuario.getPasswordHash())) {
            throw new CredencialesIncorrectasException();
        }
        AuthToken access = crearToken(usuario.getId(), TokenTipo.ACCESS, DURACION_ACCESS_TOKEN);
        AuthToken refresh = crearToken(usuario.getId(), TokenTipo.REFRESH, DURACION_REFRESH_TOKEN);
        return new ParTokens(access, refresh);
    }

    public AuthToken refresh(String refreshToken) {
        AuthToken token = authTokenRepository.findByToken(refreshToken)
                .filter(existente -> existente.getTipo() == TokenTipo.REFRESH)
                .orElseThrow(TokenInvalidoException::new);
        if (token.estaExpirado(ahora())) {
            throw new TokenExpiradoException();
        }
        return crearToken(token.getUserId(), TokenTipo.ACCESS, DURACION_ACCESS_TOKEN);
    }

    public Usuario validateAccessToken(String token) {
        AuthToken accessToken = authTokenRepository.findByToken(token)
                .filter(existente -> existente.getTipo() == TokenTipo.ACCESS)
                .orElseThrow(TokenInvalidoException::new);
        if (accessToken.estaExpirado(ahora())) {
            throw new TokenExpiradoException();
        }
        return usuarioRepository.findById(accessToken.getUserId())
                .orElseThrow(() -> new TokenInvalidoException("Token de un usuario inexistente"));
    }

    private AuthToken crearToken(Long userId, TokenTipo tipo, Duration duracion) {
        LocalDateTime ahora = ahora();
        AuthToken token = new AuthToken(null, generarToken(), userId, tipo,
                ahora.plus(duracion), ahora);
        return authTokenRepository.save(token);
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    private String generarToken() {
        byte[] bytes = new byte[LONGITUD_TOKEN];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
