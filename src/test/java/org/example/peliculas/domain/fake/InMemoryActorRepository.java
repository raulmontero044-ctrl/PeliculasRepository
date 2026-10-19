package org.example.peliculas.domain.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.example.peliculas.domain.Actor;
import org.example.peliculas.domain.repository.ActorRepository;

public class InMemoryActorRepository implements ActorRepository {

    private final Map<Long, Actor> datos = new LinkedHashMap<>();
    private final Map<Long, Long> peliculasPorActor = new HashMap<>();
    private long secuencia = 0;

    @Override
    public List<Actor> findAll() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public Optional<Actor> findById(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Actor insert(Actor actor) {
        actor.setId(++secuencia);
        datos.put(actor.getId(), actor);
        return actor;
    }

    @Override
    public Actor update(Actor actor) {
        datos.put(actor.getId(), actor);
        return actor;
    }

    @Override
    public void deleteById(Long id) {
        datos.remove(id);
    }

    @Override
    public long contarPeliculas(Long actorId) {
        return peliculasPorActor.getOrDefault(actorId, 0L);
    }

    public void setPeliculasCount(Long actorId, long cantidad) {
        peliculasPorActor.put(actorId, cantidad);
    }
}
