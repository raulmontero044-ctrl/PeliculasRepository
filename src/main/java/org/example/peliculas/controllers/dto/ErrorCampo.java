package org.example.peliculas.controllers.dto;

public class ErrorCampo {

    private final String campo;
    private final String mensaje;
    private final Object valorRechazado;

    public ErrorCampo(String campo, String mensaje, Object valorRechazado) {
        this.campo = campo;
        this.mensaje = mensaje;
        this.valorRechazado = valorRechazado;
    }

    public String getCampo() {
        return campo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Object getValorRechazado() {
        return valorRechazado;
    }
}
