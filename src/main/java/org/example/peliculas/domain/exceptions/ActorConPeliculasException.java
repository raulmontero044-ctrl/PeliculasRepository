package org.example.peliculas.domain.exceptions;

public class ActorConPeliculasException extends ReglaNegocioException {

    public ActorConPeliculasException(Long actorId) {
        super("No se puede borrar el actor " + actorId + " porque está vinculado a películas");
    }
}
