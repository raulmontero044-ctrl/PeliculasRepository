package org.example.peliculas.domain.fake;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.example.peliculas.domain.Usuario;
import org.example.peliculas.domain.repository.UsuarioRepository;

public class InMemoryUsuarioRepository implements UsuarioRepository {

    private final Map<Long, Usuario> datos = new LinkedHashMap<>();
    private long secuencia = 0;

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return datos.values().stream()
                .filter(usuario -> usuario.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(++secuencia);
        }
        datos.put(usuario.getId(), usuario);
        return usuario;
    }

    public List<Usuario> findAll() {
        return new ArrayList<>(datos.values());
    }
}
