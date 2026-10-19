package org.example.peliculas.controllers.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.peliculas.controllers.dto.ErrorRespuesta;
import org.example.peliculas.domain.exceptions.ActorNoEncontradaException;
import org.example.peliculas.domain.exceptions.PeliculaNoEncontradaException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(2)
public class RecursoNoEncontradoHandler {

    @ExceptionHandler({PeliculaNoEncontradaException.class, ActorNoEncontradaException.class})
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(RuntimeException ex,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorRespuesta.de(HttpStatus.NOT_FOUND, ex.getMessage(),
                        request.getRequestURI(), null));
    }
}
