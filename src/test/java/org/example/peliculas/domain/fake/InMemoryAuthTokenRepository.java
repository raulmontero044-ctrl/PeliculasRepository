package org.example.peliculas.domain.fake;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.example.peliculas.domain.AuthToken;
import org.example.peliculas.domain.TokenTipo;
import org.example.peliculas.domain.repository.AuthTokenRepository;

public class InMemoryAuthTokenRepository implements AuthTokenRepository {

    private final Map<Long, AuthToken> datos = new LinkedHashMap<>();
    private long secuencia = 0;

    @Override
    public AuthToken save(AuthToken token) {
        if (token.getId() == null) {
            token.setId(++secuencia);
        }
        datos.put(token.getId(), token);
        return token;
    }

    @Override
    public Optional<AuthToken> findByToken(String token) {
        return datos.values().stream()
                .filter(existente -> existente.getToken().equals(token))
                .findFirst();
    }

    @Override
    public Optional<AuthToken> findValidByToken(String token, TokenTipo tipo, LocalDateTime now) {
        return findByToken(token)
                .filter(existente -> existente.getTipo() == tipo)
                .filter(existente -> !existente.estaExpirado(now));
    }

    public List<AuthToken> findAll() {
        return new ArrayList<>(datos.values());
    }
}
