package org.example.peliculas.domain.exceptions;

public class PeliculaNoEncontradaException extends RecursoNoEncontradoException {

    public PeliculaNoEncontradaException(Long id) {
        super("No existe la película con id " + id);
    }
}
