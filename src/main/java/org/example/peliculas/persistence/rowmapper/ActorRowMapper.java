package org.example.peliculas.persistence.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.example.peliculas.domain.Actor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ActorRowMapper implements RowMapper<Actor> {

    @Override
    public Actor mapRow(ResultSet rs, int rowNum) throws SQLException {
        Actor actor = new Actor();
        actor.setId(rs.getLong("id"));
        actor.setNombre(rs.getString("nombre"));
        actor.setPais(rs.getString("pais"));
        actor.setAnioNacimiento(rs.getInt("anio_nacimiento"));
        return actor;
    }
}
