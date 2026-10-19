package org.example.peliculas.persistence;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.example.peliculas.domain.Actor;
import org.example.peliculas.domain.repository.ActorRepository;
import org.example.peliculas.persistence.rowmapper.PeliculaRowMapper;
import org.example.peliculas.persistence.rowmapper.ActorRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ActorDao implements ActorRepository {

    private static final String COLUMNAS = "id, nombre, pais, anio_nacimiento";

    private final JdbcTemplate jdbcTemplate;
    private final ActorRowMapper actorRowMapper;
    private final PeliculaRowMapper peliculaRowMapper;

    public ActorDao(JdbcTemplate jdbcTemplate,
                    ActorRowMapper actorRowMapper,
                    PeliculaRowMapper peliculaRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.actorRowMapper = actorRowMapper;
        this.peliculaRowMapper = peliculaRowMapper;
    }

    @Override
    public List<Actor> findAll() {
        List<Actor> actores = jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM actores ORDER BY id", actorRowMapper);
        cargarPeliculas(actores);
        return actores;
    }

    @Override
    public Optional<Actor> findById(Long id) {
        List<Actor> resultado = jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM actores WHERE id = ?", actorRowMapper, id);
        cargarPeliculas(resultado);
        return resultado.stream().findFirst();
    }

    @Override
    public Actor insert(Actor actor) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO actores (nombre, pais, anio_nacimiento) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, actor.getNombre());
            ps.setString(2, actor.getPais());
            ps.setInt(3, actor.getAnioNacimiento());
            return ps;
        }, keyHolder);
        actor.setId(keyHolder.getKey().longValue());
        return actor;
    }

    @Override
    public Actor update(Actor actor) {
        jdbcTemplate.update(
                "UPDATE actores SET nombre = ?, pais = ?, anio_nacimiento = ? WHERE id = ?",
                actor.getNombre(),
                actor.getPais(),
                actor.getAnioNacimiento(),
                actor.getId());
        return actor;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM actores WHERE id = ?", id);
    }

    @Override
    public long contarPeliculas(Long actorId) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pelicula_actor WHERE actor_id = ?", Long.class, actorId);
        return total == null ? 0L : total;
    }

    private void cargarPeliculas(List<Actor> actores) {
        if (actores.isEmpty()) {
            return;
        }
        Map<Long, Actor> porId = actores.stream()
                .collect(Collectors.toMap(Actor::getId, Function.identity()));
        jdbcTemplate.query(
                "SELECT pa.actor_id, p.id, p.titulo, p.anio, p.director, p.genero, "
                        + "p.duracion_minutos, p.sinopsis "
                        + "FROM pelicula_actor pa JOIN peliculas p ON p.id = pa.pelicula_id",
                rs -> {
                    Actor actor = porId.get(rs.getLong("actor_id"));
                    if (actor != null) {
                        actor.getPeliculas().add(peliculaRowMapper.mapRow(rs, 0));
                    }
                });
    }
}
