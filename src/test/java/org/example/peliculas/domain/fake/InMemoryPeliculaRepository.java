package org.example.peliculas.domain.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.example.peliculas.domain.Pelicula;
import org.example.peliculas.domain.repository.PeliculaRepository;

public class InMemoryPeliculaRepository implements PeliculaRepository {

    private final Map<Long, Pelicula> datos = new LinkedHashMap<>();
    private final Map<Long, Set<Long>> actoresPorPelicula = new HashMap<>();
    private long secuencia = 0;

    @Override
    public List<Pelicula> findAll() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public Optional<Pelicula> findById(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Optional<Pelicula> findByTituloAndAnio(String titulo, Integer anio) {
        return datos.values().stream()
                .filter(p -> Objects.equals(p.getTitulo(), titulo) && Objects.equals(p.getAnio(), anio))
                .findFirst();
    }

    @Override
    public Pelicula insert(Pelicula pelicula) {
        pelicula.setId(++secuencia);
        datos.put(pelicula.getId(), pelicula);
        return pelicula;
    }

    @Override
    public Pelicula update(Pelicula pelicula) {
        datos.put(pelicula.getId(), pelicula);
        return pelicula;
    }

    @Override
    public void deleteById(Long id) {
        datos.remove(id);
        actoresPorPelicula.remove(id);
    }

    @Override
    public boolean existeActorAsignado(Long peliculaId, Long actorId) {
        return actoresPorPelicula.getOrDefault(peliculaId, Set.of()).contains(actorId);
    }

    @Override
    public void asignarActor(Long peliculaId, Long actorId) {
        actoresPorPelicula.computeIfAbsent(peliculaId, k -> new HashSet<>()).add(actorId);
    }

    @Override
    public void quitarActor(Long peliculaId, Long actorId) {
        actoresPorPelicula.getOrDefault(peliculaId, new HashSet<>()).remove(actorId);
    }
}
