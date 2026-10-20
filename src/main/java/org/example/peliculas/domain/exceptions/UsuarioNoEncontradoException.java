package org.example.peliculas.domain.exceptions;

public class UsuarioNoEncontradoException extends AutenticacionException {

    public UsuarioNoEncontradoException() {
        super("Usuario no existe");
    }
}
