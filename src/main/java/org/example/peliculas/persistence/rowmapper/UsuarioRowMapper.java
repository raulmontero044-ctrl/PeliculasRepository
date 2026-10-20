package org.example.peliculas.persistence.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.example.peliculas.domain.Usuario;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRowMapper implements RowMapper<Usuario> {

    @Override
    public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setEnabled(rs.getBoolean("enabled"));
        return usuario;
    }
}
