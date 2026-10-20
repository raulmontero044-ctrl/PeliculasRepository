package org.example.auth.domain.exceptions;

public class CredencialesIncorrectasException extends AutenticacionException {

    public CredencialesIncorrectasException() {
        super("Credenciales incorrectas");
    }
}
