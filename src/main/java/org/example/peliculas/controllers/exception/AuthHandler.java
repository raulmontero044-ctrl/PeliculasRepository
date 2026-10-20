package org.example.peliculas.controllers.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.peliculas.controllers.dto.ErrorRespuesta;
import org.example.peliculas.domain.exceptions.CredencialesIncorrectasException;
import org.example.peliculas.domain.exceptions.TokenExpiradoException;
import org.example.peliculas.domain.exceptions.TokenInvalidoException;
import org.example.peliculas.domain.exceptions.UsuarioNoEncontradoException;
import org.example.peliculas.domain.exceptions.UsuarioYaExisteException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(5)
public class AuthHandler {

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<ErrorRespuesta> manejarUsuarioRepetido(UsuarioYaExisteException ex,
                                                                 HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler({UsuarioNoEncontradoException.class, CredencialesIncorrectasException.class,
            TokenInvalidoException.class, TokenExpiradoException.class})
    public ResponseEntity<ErrorRespuesta> manejarAutenticacion(RuntimeException ex,
                                                               HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, ex, request);
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, RuntimeException ex,
                                                     HttpServletRequest request) {
        return ResponseEntity.status(estado)
                .body(ErrorRespuesta.de(estado, ex.getMessage(), request.getRequestURI(), null));
    }
}
