package org.example.peliculas.domain.repository;

import java.util.List;
import java.util.Optional;
import org.example.peliculas.domain.Actor;

public interface ActorRepository {

    List<Actor> findAll();

    Optional<Actor> findById(Long id);

    Actor insert(Actor actor);

    Actor update(Actor actor);

    void deleteById(Long id);

    long contarPeliculas(Long actorId);
}
