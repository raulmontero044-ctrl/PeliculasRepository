package org.example.peliculas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.example.peliculas.domain.Actor;
import org.example.peliculas.domain.repository.ActorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ActorDaoTest {

    @Autowired
    private ActorRepository actorRepository;

    private Actor nuevo(String nombre) {
        return new Actor(null, nombre, "Pais", 1970);
    }

    @Test
    void findAll_devuelveLosDatosSemilla() {
        List<Actor> actores = actorRepository.findAll();

        assertEquals(4, actores.size());
    }

    @Test
    void findById_devuelveActorConSusPeliculas() {
        Actor actor = actorRepository.findById(1L).orElseThrow();

        assertEquals("Tom Hanks", actor.getNombre());
        assertEquals(1, actor.getPeliculas().size());
    }

    @Test
    void insert_persisteAsignandoId() {
        Actor guardado = actorRepository.insert(nuevo("Actor Nuevo"));

        assertTrue(guardado.getId() != null);
        assertEquals("Actor Nuevo", actorRepository.findById(guardado.getId()).orElseThrow().getNombre());
    }

    @Test
    void update_modificaElActor() {
        Actor actor = actorRepository.findById(1L).orElseThrow();
        actor.setPais("Canada");

        actorRepository.update(actor);

        assertEquals("Canada", actorRepository.findById(1L).orElseThrow().getPais());
    }

    @Test
    void contarPeliculas_cuentaLasRelaciones() {
        assertEquals(1L, actorRepository.contarPeliculas(1L));
        assertEquals(0L, actorRepository.contarPeliculas(999L));
    }

    @Test
    void deleteById_eliminaElActor() {
        Actor creado = actorRepository.insert(nuevo("Para Borrar"));

        actorRepository.deleteById(creado.getId());

        assertTrue(actorRepository.findById(creado.getId()).isEmpty());
    }
}
