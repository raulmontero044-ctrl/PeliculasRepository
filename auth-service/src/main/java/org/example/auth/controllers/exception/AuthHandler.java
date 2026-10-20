package org.example.auth.controllers.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.auth.controllers.dto.ErrorRespuesta;
import org.example.auth.domain.exceptions.CredencialesIncorrectasException;
import org.example.auth.domain.exceptions.TokenExpiradoException;
import org.example.auth.domain.exceptions.TokenInvalidoException;
import org.example.auth.domain.exceptions.UsuarioNoEncontradoException;
import org.example.auth.domain.exceptions.UsuarioYaExisteException;
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

    @ExceptionHandler(TokenExpiradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarTokenExpirado(TokenExpiradoException ex,
                                                               HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorRespuesta.deConMotivo(HttpStatus.UNAUTHORIZED, ex.getMessage(),
                        request.getRequestURI(), "TOKEN_EXPIRADO"));
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorRespuesta> manejarTokenInvalido(TokenInvalidoException ex,
                                                               HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorRespuesta.deConMotivo(HttpStatus.UNAUTHORIZED, ex.getMessage(),
                        request.getRequestURI(), "TOKEN_INVALIDO"));
    }

    @ExceptionHandler({UsuarioNoEncontradoException.class, CredencialesIncorrectasException.class})
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
