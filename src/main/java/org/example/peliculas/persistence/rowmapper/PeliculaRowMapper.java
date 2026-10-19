package org.example.peliculas.persistence.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.example.peliculas.domain.Genero;
import org.example.peliculas.domain.Pelicula;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class PeliculaRowMapper implements RowMapper<Pelicula> {

    @Override
    public Pelicula mapRow(ResultSet rs, int rowNum) throws SQLException {
        Pelicula pelicula = new Pelicula();
        pelicula.setId(rs.getLong("id"));
        pelicula.setTitulo(rs.getString("titulo"));
        pelicula.setAnio(rs.getInt("anio"));
        pelicula.setDirector(rs.getString("director"));
        pelicula.setGenero(Genero.valueOf(rs.getString("genero")));
        pelicula.setDuracionMinutos(rs.getInt("duracion_minutos"));
        pelicula.setSinopsis(rs.getString("sinopsis"));
        return pelicula;
    }
}
