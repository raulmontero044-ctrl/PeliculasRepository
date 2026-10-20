package org.example.peliculas.domain.repository;

import java.util.Optional;
import org.example.peliculas.domain.Usuario;

public interface UsuarioRepository {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findById(Long id);

    boolean existsByUsername(String username);

    Usuario save(Usuario usuario);
}
