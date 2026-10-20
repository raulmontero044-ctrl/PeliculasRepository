package org.example.peliculas.controllers.config;

import org.example.peliculas.domain.repository.ActorRepository;
import org.example.peliculas.domain.repository.PeliculaRepository;
import org.example.peliculas.domain.service.ActorService;
import org.example.peliculas.domain.service.PeliculaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
