package org.example.peliculas.domain.exceptions;

public class ActorNoEncontradaException extends RecursoNoEncontradoException {

    public ActorNoEncontradaException(Long id) {
        super("No existe el actor con id " + id);
    }
}
