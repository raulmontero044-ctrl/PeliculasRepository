package org.example.peliculas.domain.service;

import java.util.List;
import org.example.peliculas.domain.Actor;
import org.example.peliculas.domain.Pelicula;
import org.example.peliculas.domain.exceptions.ActorNoEncontradaException;
import org.example.peliculas.domain.exceptions.ActorYaAsignadoException;
import org.example.peliculas.domain.exceptions.PeliculaDuplicadaException;
import org.example.peliculas.domain.exceptions.PeliculaNoEncontradaException;
import org.example.peliculas.domain.repository.ActorRepository;
import org.example.peliculas.domain.repository.PeliculaRepository;

public class PeliculaService {

    private final PeliculaRepository peliculaRepository;
    private final ActorRepository actorRepository;

    public PeliculaService(PeliculaRepository peliculaRepository, ActorRepository actorRepository) {
        this.peliculaRepository = peliculaRepository;
        this.actorRepository = actorRepository;
    }

    public List<Pelicula> listar() {
        return peliculaRepository.findAll();
    }

    public Pelicula obtener(Long id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new PeliculaNoEncontradaException(id));
    }

    public Pelicula crear(Pelicula pelicula) {
        comprobarDuplicado(pelicula.getTitulo(), pelicula.getAnio(), null);
        return peliculaRepository.insert(pelicula);
    }

    public Pelicula actualizar(Long id, Pelicula datos) {
        Pelicula existente = obtener(id);
        comprobarDuplicado(datos.getTitulo(), datos.getAnio(), id);
        existente.setTitulo(datos.getTitulo());
        existente.setAnio(datos.getAnio());
        existente.setDirector(datos.getDirector());
        existente.setGenero(datos.getGenero());
        existente.setDuracionMinutos(datos.getDuracionMinutos());
        existente.setSinopsis(datos.getSinopsis());
        return peliculaRepository.update(existente);
    }

    public void eliminar(Long id) {
        obtener(id);
        peliculaRepository.deleteById(id);
    }

    public Pelicula asignarActor(Long peliculaId, Long actorId) {
        obtener(peliculaId);
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ActorNoEncontradaException(actorId));
        if (peliculaRepository.existeActorAsignado(peliculaId, actorId)) {
            throw new ActorYaAsignadoException(peliculaId, actorId);
        }
        peliculaRepository.asignarActor(peliculaId, actor.getId());
        return obtener(peliculaId);
    }

    private void comprobarDuplicado(String titulo, Integer anio, Long idExcluido) {
        peliculaRepository.findByTituloAndAnio(titulo, anio)
                .filter(otra -> idExcluido == null || !otra.getId().equals(idExcluido))
                .ifPresent(otra -> {
                    throw new PeliculaDuplicadaException(titulo, anio);
                });
    }
}
