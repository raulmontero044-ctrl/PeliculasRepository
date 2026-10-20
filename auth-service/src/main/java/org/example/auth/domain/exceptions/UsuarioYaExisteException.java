package org.example.auth.domain.exceptions;

public class UsuarioYaExisteException extends AutenticacionException {

    public UsuarioYaExisteException(String username) {
        super("Ya existe un usuario con nombre '" + username + "'");
    }
}
