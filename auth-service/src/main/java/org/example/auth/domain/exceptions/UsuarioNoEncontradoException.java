package org.example.auth.domain.exceptions;

public class UsuarioNoEncontradoException extends AutenticacionException {

    public UsuarioNoEncontradoException() {
        super("Usuario no existe");
    }
}
