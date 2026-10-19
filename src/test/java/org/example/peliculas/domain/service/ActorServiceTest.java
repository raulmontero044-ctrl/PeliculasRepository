package org.example.peliculas.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.peliculas.domain.Actor;
import org.example.peliculas.domain.exceptions.ActorConPeliculasException;
import org.example.peliculas.domain.exceptions.ActorNoEncontradaException;
import org.example.peliculas.domain.fake.InMemoryActorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActorServiceTest {

    private InMemoryActorRepository actores;
    private ActorService service;

    @BeforeEach
    void setUp() {
        actores = new InMemoryActorRepository();
        service = new ActorService(actores);
    }

    private Actor actor(String nombre) {
        return new Actor(null, nombre, "España", 1980);
    }

    @Test
    void listar_devuelveTodosLosActores() {
        actores.insert(actor("Uno"));
        actores.insert(actor("Dos"));

        assertEquals(2, service.listar().size());
    }

    @Test
    void obtener_existente_devuelveActor() {
        Actor creado = actores.insert(actor("Uno"));

        assertEquals("Uno", service.obtener(creado.getId()).getNombre());
    }

    @Test
    void obtener_inexistente_lanzaActorNoEncontrada() {
        assertThrows(ActorNoEncontradaException.class, () -> service.obtener(99L));
    }

    @Test
    void crear_nuevo_asignaId() {
        Actor resultado = service.crear(actor("Nuevo"));

        assertTrue(resultado.getId() != null);
    }

    @Test
    void eliminar_sinPeliculas_borraActor() {
        Actor creado = service.crear(actor("Sin pelis"));

        service.eliminar(creado.getId());

        assertTrue(actores.findById(creado.getId()).isEmpty());
    }

    @Test
    void eliminar_conPeliculas_lanzaActorConPeliculas() {
        Actor creado = service.crear(actor("Con pelis"));
        actores.setPeliculasCount(creado.getId(), 2L);

        assertThrows(ActorConPeliculasException.class, () -> service.eliminar(creado.getId()));
    }

    @Test
    void eliminar_inexistente_lanzaActorNoEncontrada() {
        assertThrows(ActorNoEncontradaException.class, () -> service.eliminar(99L));
    }
}
