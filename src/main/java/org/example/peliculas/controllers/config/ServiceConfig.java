package org.example.peliculas.controllers.config;

import java.time.Clock;
import org.example.peliculas.domain.repository.ActorRepository;
import org.example.peliculas.domain.repository.AuthTokenRepository;
import org.example.peliculas.domain.repository.PeliculaRepository;
import org.example.peliculas.domain.repository.PasswordHasher;
import org.example.peliculas.domain.repository.UsuarioRepository;
import org.example.peliculas.domain.service.ActorService;
import org.example.peliculas.domain.service.AuthService;
import org.example.peliculas.domain.service.PeliculaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class ServiceConfig {

    @Bean
    public PeliculaService peliculaService(PeliculaRepository peliculaRepository,
                                           ActorRepository actorRepository) {
        return new PeliculaService(peliculaRepository, actorRepository);
    }

    @Bean
    public ActorService actorService(ActorRepository actorRepository) {
        return new ActorService(actorRepository);
    }

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
