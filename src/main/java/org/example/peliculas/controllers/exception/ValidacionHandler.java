package org.example.peliculas.controllers.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.example.peliculas.controllers.dto.ErrorCampo;
import org.example.peliculas.controllers.dto.ErrorRespuesta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Order(1)
public class ValidacionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarCuerpoInvalido(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        List<ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorCampo(error.getField(), error.getDefaultMessage(),
                        error.getRejectedValue()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", request, errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarRestriccion(ConstraintViolationException ex,
                                                             HttpServletRequest request) {
        List<ErrorCampo> errores = ex.getConstraintViolations().stream()
                .map(violacion -> new ErrorCampo(violacion.getPropertyPath().toString(),
                        violacion.getMessage(), violacion.getInvalidValue()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Restricción de validación incumplida", request, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarNoLeible(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "JSON malformado o valor no válido", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "Parámetro inválido: " + ex.getName(), request, null);
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String mensaje,
                                                     HttpServletRequest request, List<ErrorCampo> errores) {
        return ResponseEntity.status(estado)
                .body(ErrorRespuesta.de(estado, mensaje, request.getRequestURI(), errores));
    }
}
