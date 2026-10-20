package org.example.peliculas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.peliculas.domain.Usuario;
import org.example.peliculas.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioDaoTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario nuevo(String username) {
        return new Usuario(null, username, "hash-" + username, true);
    }

    @Test
    void save_insertaAsignandoId() {
        Usuario guardado = usuarioRepository.save(nuevo("raul"));

        assertTrue(guardado.getId() != null);
        assertEquals("raul", usuarioRepository.findById(guardado.getId()).orElseThrow().getUsername());
    }

    @Test
    void save_actualizaElUsuarioExistente() {
        Usuario guardado = usuarioRepository.save(nuevo("raul"));
        guardado.setPasswordHash("hash-nuevo");
        guardado.setEnabled(false);

        usuarioRepository.save(guardado);

        Usuario recargado = usuarioRepository.findById(guardado.getId()).orElseThrow();
        assertEquals("hash-nuevo", recargado.getPasswordHash());
        assertFalse(recargado.isEnabled());
    }

    @Test
    void findByUsername_devuelveElUsuario() {
        usuarioRepository.save(nuevo("raul"));

        assertEquals("hash-raul",
                usuarioRepository.findByUsername("raul").orElseThrow().getPasswordHash());
    }

    @Test
    void findByUsername_inexistente_devuelveVacio() {
        assertTrue(usuarioRepository.findByUsername("nadie").isEmpty());
    }

    @Test
    void existsByUsername_reflejaLaExistencia() {
        assertFalse(usuarioRepository.existsByUsername("raul"));

        usuarioRepository.save(nuevo("raul"));

        assertTrue(usuarioRepository.existsByUsername("raul"));
    }

    @Test
    void save_usernameRepetido_lanzaViolacionDeUnicidad() {
        usuarioRepository.save(nuevo("raul"));

        assertThrows(DataIntegrityViolationException.class,
                () -> usuarioRepository.save(nuevo("raul")));
    }
}
