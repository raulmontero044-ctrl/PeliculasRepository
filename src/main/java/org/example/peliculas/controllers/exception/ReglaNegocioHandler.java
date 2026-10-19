package org.example.peliculas.controllers.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.peliculas.controllers.dto.ErrorRespuesta;
import org.example.peliculas.domain.exceptions.ActorConPeliculasException;
import org.example.peliculas.domain.exceptions.ActorYaAsignadoException;
import org.example.peliculas.domain.exceptions.PeliculaDuplicadaException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(3)
public class ReglaNegocioHandler {

    @ExceptionHandler({PeliculaDuplicadaException.class, ActorYaAsignadoException.class,
            ActorConPeliculasException.class})
    public ResponseEntity<ErrorRespuesta> manejarReglaNegocio(RuntimeException ex,
                                                             HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorRespuesta.de(HttpStatus.CONFLICT, ex.getMessage(),
                        request.getRequestURI(), null));
    }
}
