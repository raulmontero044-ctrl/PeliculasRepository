package org.example.peliculas.domain.exceptions;

public class ActorYaAsignadoException extends ReglaNegocioException {

    public ActorYaAsignadoException(Long peliculaId, Long actorId) {
        super("El actor " + actorId + " ya está asignado a la película " + peliculaId);
    }
}
