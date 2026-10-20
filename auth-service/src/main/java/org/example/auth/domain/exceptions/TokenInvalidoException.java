package org.example.auth.domain.exceptions;

public class TokenInvalidoException extends AutenticacionException {

    public TokenInvalidoException() {
        this("Token inválido");
    }

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
