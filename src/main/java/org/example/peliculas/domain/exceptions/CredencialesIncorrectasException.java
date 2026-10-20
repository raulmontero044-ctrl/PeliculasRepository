package org.example.peliculas.domain.exceptions;

public class CredencialesIncorrectasException extends AutenticacionException {

    public CredencialesIncorrectasException() {
        super("Credenciales incorrectas");
    }
}
