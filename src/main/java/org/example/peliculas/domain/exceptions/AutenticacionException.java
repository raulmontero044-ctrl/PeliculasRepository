package org.example.peliculas.domain.exceptions;

public abstract class AutenticacionException extends RuntimeException {

    protected AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
