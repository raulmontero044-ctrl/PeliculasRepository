package org.example.peliculas.domain.repository;

import java.util.List;
import java.util.Optional;
import org.example.peliculas.domain.Pelicula;

public interface PeliculaRepository {

    List<Pelicula> findAll();

    Optional<Pelicula> findById(Long id);

    Optional<Pelicula> findByTituloAndAnio(String titulo, Integer anio);

    Pelicula insert(Pelicula pelicula);

    Pelicula update(Pelicula pelicula);

    void deleteById(Long id);

    boolean existeActorAsignado(Long peliculaId, Long actorId);

    void asignarActor(Long peliculaId, Long actorId);

    void quitarActor(Long peliculaId, Long actorId);
}
