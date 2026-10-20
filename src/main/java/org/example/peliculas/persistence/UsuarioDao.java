package org.example.peliculas.persistence;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.example.peliculas.domain.Usuario;
import org.example.peliculas.domain.repository.UsuarioRepository;
import org.example.peliculas.persistence.rowmapper.UsuarioRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDao implements UsuarioRepository {

    private static final String COLUMNAS = "id, username, password_hash, enabled";

    private final JdbcTemplate jdbcTemplate;
    private final UsuarioRowMapper usuarioRowMapper;

    public UsuarioDao(JdbcTemplate jdbcTemplate, UsuarioRowMapper usuarioRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.usuarioRowMapper = usuarioRowMapper;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        List<Usuario> resultado = jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM usuarios WHERE username = ?",
                usuarioRowMapper, username);
        return resultado.stream().findFirst();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        List<Usuario> resultado = jdbcTemplate.query(
                "SELECT " + COLUMNAS + " FROM usuarios WHERE id = ?", usuarioRowMapper, id);
        return resultado.stream().findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE username = ?", Integer.class, username);
        return total != null && total > 0;
    }

    @Override
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            return insert(usuario);
        }
        jdbcTemplate.update(
                "UPDATE usuarios SET username = ?, password_hash = ?, enabled = ? WHERE id = ?",
                usuario.getUsername(),
                usuario.getPasswordHash(),
                usuario.isEnabled(),
                usuario.getId());
        return usuario;
    }

    private Usuario insert(Usuario usuario) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO usuarios (username, password_hash, enabled) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setBoolean(3, usuario.isEnabled());
            return ps;
        }, keyHolder);
        usuario.setId(keyHolder.getKey().longValue());
        return usuario;
    }
}
