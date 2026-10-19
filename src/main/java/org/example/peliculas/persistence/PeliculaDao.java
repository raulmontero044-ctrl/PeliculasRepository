package org.example.peliculas.persistence;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.example.peliculas.domain.Pelicula;
import org.example.peliculas.domain.repository.PeliculaRepository;
import org.example.peliculas.persistence.rowmapper.ActorRowMapper;
import org.example.peliculas.persistence.rowmapper.PeliculaRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PeliculaDao implements PeliculaRepository {

    private static final String COLUMNAS =
            "id, titulo, anio, director, genero, duracion_minutos, sinopsis";

    private final JdbcTemplate jdbcTemplate;
    private final PeliculaRowMapper peliculaRowMapper;
    private final ActorRowMapper actorRowMapper;

    public PeliculaDao(JdbcTemplate jdbcTemplate,
                       PeliculaRowMapper peliculaRowMapper,
                       ActorRowMapper actorRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.peliculaRowMapper = peliculaRowMapper;
        this.actorRowMapper = actorRowMapper;
    }

    @Override
    public List<Pelicula> findAll() {
        List<Pelicula> peliculas = jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM peliculas ORDER BY id", peliculaRowMapper);
        cargarActores(peliculas);
        return peliculas;
    }

    @Override
    public Optional<Pelicula> findById(Long id) {
        List<Pelicula> resultado = jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM peliculas WHERE id = ?", peliculaRowMapper, id);
        cargarActores(resultado);
        return resultado.stream().findFirst();
    }

    @Override
    public Optional<Pelicula> findByTituloAndAnio(String titulo, Integer anio) {
        return jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM peliculas WHERE titulo = ? AND anio = ?",
                peliculaRowMapper, titulo, anio).stream().findFirst();
    }

    @Override
    public Pelicula insert(Pelicula pelicula) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO peliculas (titulo, anio, director, genero, duracion_minutos, sinopsis) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, pelicula.getTitulo());
            ps.setInt(2, pelicula.getAnio());
            ps.setString(3, pelicula.getDirector());
            ps.setString(4, pelicula.getGenero().name());
            ps.setInt(5, pelicula.getDuracionMinutos());
            ps.setString(6, pelicula.getSinopsis());
            return ps;
        }, keyHolder);
        pelicula.setId(keyHolder.getKey().longValue());
        return pelicula;
    }

    @Override
    public Pelicula update(Pelicula pelicula) {
        jdbcTemplate.update(
                "UPDATE peliculas SET titulo = ?, anio = ?, director = ?, genero = ?, "
                        + "duracion_minutos = ?, sinopsis = ? WHERE id = ?",
                pelicula.getTitulo(),
                pelicula.getAnio(),
                pelicula.getDirector(),
                pelicula.getGenero().name(),
                pelicula.getDuracionMinutos(),
                pelicula.getSinopsis(),
                pelicula.getId());
        return pelicula;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM pelicula_actor WHERE pelicula_id = ?", id);
        jdbcTemplate.update("DELETE FROM peliculas WHERE id = ?", id);
    }

    @Override
    public boolean existeActorAsignado(Long peliculaId, Long actorId) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pelicula_actor WHERE pelicula_id = ? AND actor_id = ?",
                Integer.class, peliculaId, actorId);
        return total != null && total > 0;
    }

    @Override
    public void asignarActor(Long peliculaId, Long actorId) {
        jdbcTemplate.update(
                "INSERT INTO pelicula_actor (pelicula_id, actor_id) VALUES (?, ?)",
                peliculaId, actorId);
    }

    @Override
    public void quitarActor(Long peliculaId, Long actorId) {
        jdbcTemplate.update(
                "DELETE FROM pelicula_actor WHERE pelicula_id = ? AND actor_id = ?",
                peliculaId, actorId);
    }

    private void cargarActores(List<Pelicula> peliculas) {
        if (peliculas.isEmpty()) {
            return;
        }
        Map<Long, Pelicula> porId = peliculas.stream()
                .collect(Collectors.toMap(Pelicula::getId, Function.identity()));
        jdbcTemplate.query(
                "SELECT pa.pelicula_id, a.id, a.nombre, a.pais, a.anio_nacimiento "
                        + "FROM pelicula_actor pa JOIN actores a ON a.id = pa.actor_id",
                rs -> {
                    Pelicula pelicula = porId.get(rs.getLong("pelicula_id"));
                    if (pelicula != null) {
                        pelicula.getActores().add(actorRowMapper.mapRow(rs, 0));
                    }
                });
    }
}
