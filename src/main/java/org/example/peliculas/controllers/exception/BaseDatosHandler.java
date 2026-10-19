package org.example.peliculas.controllers.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.peliculas.controllers.dto.ErrorRespuesta;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(4)
public class BaseDatosHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(DataIntegrityViolationException ex,
                                                            HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, "Violación de integridad en base de datos", request);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ErrorRespuesta> manejarRecurso(DataAccessResourceFailureException ex,
                                                        HttpServletRequest request) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de base de datos no disponible", request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorRespuesta> manejarAcceso(DataAccessException ex,
                                                       HttpServletRequest request) {
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error de acceso a datos", request);
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String mensaje,
                                                     HttpServletRequest request) {
        return ResponseEntity.status(estado)
                .body(ErrorRespuesta.de(estado, mensaje, request.getRequestURI(), null));
    }
}
