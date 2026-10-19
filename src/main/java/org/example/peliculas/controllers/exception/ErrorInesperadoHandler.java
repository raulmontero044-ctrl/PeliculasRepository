package org.example.peliculas.controllers.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.peliculas.controllers.dto.ErrorRespuesta;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ErrorInesperadoHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarInesperado(Exception ex,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorRespuesta.de(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error inesperado en el servidor", request.getRequestURI(), null));
    }
}
