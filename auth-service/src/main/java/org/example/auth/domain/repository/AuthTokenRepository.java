package org.example.auth.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.example.auth.domain.AuthToken;
import org.example.auth.domain.TokenTipo;

public interface AuthTokenRepository {

    AuthToken save(AuthToken token);

    Optional<AuthToken> findByToken(String token);

    Optional<AuthToken> findValidByToken(String token, TokenTipo tipo, LocalDateTime now);
}
