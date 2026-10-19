package org.example.peliculas.domain.exceptions;

public abstract class RecursoNoEncontradoException extends RuntimeException {

    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
