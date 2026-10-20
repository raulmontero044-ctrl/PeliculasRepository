package org.example.peliculas.domain.exceptions;

public class AuthNoDisponibleException extends RuntimeException {

    public AuthNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public AuthNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
