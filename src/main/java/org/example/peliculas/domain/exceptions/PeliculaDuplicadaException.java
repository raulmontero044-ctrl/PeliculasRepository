package org.example.peliculas.domain.exceptions;

public class PeliculaDuplicadaException extends ReglaNegocioException {

    public PeliculaDuplicadaException(String titulo, Integer anio) {
        super("Ya existe una película con título '" + titulo + "' y año " + anio);
    }
}
