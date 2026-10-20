package org.example.auth.domain.exceptions;

public abstract class AutenticacionException extends RuntimeException {

    protected AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
