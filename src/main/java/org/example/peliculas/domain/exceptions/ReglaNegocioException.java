package org.example.peliculas.domain.exceptions;

public abstract class ReglaNegocioException extends RuntimeException {

    protected ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
