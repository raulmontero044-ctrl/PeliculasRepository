package org.example.peliculas.domain.service;

import java.util.List;
import org.example.peliculas.domain.Actor;
import org.example.peliculas.domain.exceptions.ActorConPeliculasException;
import org.example.peliculas.domain.exceptions.ActorNoEncontradaException;
import org.example.peliculas.domain.repository.ActorRepository;

public class ActorService {

    private final ActorRepository actorRepository;

    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    public List<Actor> listar() {
        return actorRepository.findAll();
    }

    public Actor obtener(Long id) {
        return actorRepository.findById(id)
                .orElseThrow(() -> new ActorNoEncontradaException(id));
    }

    public Actor crear(Actor actor) {
        return actorRepository.insert(actor);
    }

    public void eliminar(Long id) {
        obtener(id);
        if (actorRepository.contarPeliculas(id) > 0) {
            throw new ActorConPeliculasException(id);
        }
        actorRepository.deleteById(id);
    }
}
