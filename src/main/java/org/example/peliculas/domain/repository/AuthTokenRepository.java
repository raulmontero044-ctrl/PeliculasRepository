package org.example.peliculas.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.example.peliculas.domain.AuthToken;
import org.example.peliculas.domain.TokenTipo;

public interface AuthTokenRepository {

    AuthToken save(AuthToken token);

    Optional<AuthToken> findByToken(String token);

    Optional<AuthToken> findValidByToken(String token, TokenTipo tipo, LocalDateTime now);
}
