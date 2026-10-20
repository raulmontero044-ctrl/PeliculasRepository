package org.example.auth.domain.exceptions;

public class TokenExpiradoException extends AutenticacionException {

    public TokenExpiradoException() {
        this("Token expirado");
    }

    public TokenExpiradoException(String mensaje) {
        super(mensaje);
    }
}
