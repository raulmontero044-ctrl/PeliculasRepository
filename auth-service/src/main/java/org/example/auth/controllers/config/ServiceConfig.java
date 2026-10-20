package org.example.auth.controllers.config;

import java.time.Clock;
import org.example.auth.domain.repository.AuthTokenRepository;
import org.example.auth.domain.repository.PasswordHasher;
import org.example.auth.domain.repository.UsuarioRepository;
import org.example.auth.domain.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class ServiceConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordHasher passwordHasher(BCryptPasswordEncoder encoder) {
        return new BCryptPasswordHasher(encoder);
    }

    @Bean
    public AuthService authService(UsuarioRepository usuarioRepository,
                                   AuthTokenRepository authTokenRepository,
                                   PasswordHasher passwordHasher) {
        return new AuthService(usuarioRepository, authTokenRepository, passwordHasher,
                Clock.systemDefaultZone());
    }
}
