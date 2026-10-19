package org.example.peliculas.controllers.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;

public class ErrorRespuesta {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String mensaje;
    private final String path;
    private final List<ErrorCampo> errores;

    public ErrorRespuesta(LocalDateTime timestamp, int status, String error, String mensaje,
                          String path, List<ErrorCampo> errores) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
        this.errores = errores;
    }

    public static ErrorRespuesta de(HttpStatus estado, String mensaje, String path,
                                    List<ErrorCampo> errores) {
        return new ErrorRespuesta(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(),
                mensaje, path, errores);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getPath() {
        return path;
    }

    public List<ErrorCampo> getErrores() {
        return errores;
    }
}
