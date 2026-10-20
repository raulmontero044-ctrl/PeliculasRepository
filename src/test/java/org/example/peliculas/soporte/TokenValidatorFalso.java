package org.example.peliculas.soporte;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.example.peliculas.domain.exceptions.AuthNoDisponibleException;
import org.example.peliculas.domain.exceptions.TokenExpiradoException;
import org.example.peliculas.domain.exceptions.TokenInvalidoException;
import org.example.peliculas.domain.repository.TokenValidator;

public class TokenValidatorFalso implements TokenValidator {

    public static final String MENSAJE_NO_DISPONIBLE = "Servicio de autenticación no disponible";

    private final Map<String, LocalDateTime> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> refreshTokens = new ConcurrentHashMap<>();
    private boolean caido;

    public String emitir(String username) {
        return emitirConVida(username, accessTokens, LocalDateTime.now().plusMinutes(5));
    }

    public String emitirExpirado(String username) {
        return emitirConVida(username, accessTokens, LocalDateTime.now().minusMinutes(1));
    }

    public String emitirRefresh(String username) {
        return emitirConVida(username, refreshTokens, LocalDateTime.now().plusDays(1));
    }

    public void caer() {
        caido = true;
    }

    public void reactivar() {
        caido = false;
    }

    @Override
    public void validar(String accessToken) {
        if (caido) {
            throw new AuthNoDisponibleException(MENSAJE_NO_DISPONIBLE);
        }
        if (refreshTokens.containsKey(accessToken)) {
            throw new TokenInvalidoException();
        }
        LocalDateTime expira = accessTokens.get(accessToken);
        if (expira == null) {
            throw new TokenInvalidoException();
        }
        if (expira.isBefore(LocalDateTime.now())) {
            throw new TokenExpiradoException();
        }
    }

    private String emitirConVida(String username, Map<String, LocalDateTime> destino,
                                 LocalDateTime expiracion) {
        String token = "token-" + username + "-" + UUID.randomUUID();
        destino.put(token, expiracion);
        return token;
    }
}
