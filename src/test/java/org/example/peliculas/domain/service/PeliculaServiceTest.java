package org.example.peliculas.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.example.peliculas.domain.Actor;
import org.example.peliculas.domain.Genero;
import org.example.peliculas.domain.Pelicula;
import org.example.peliculas.domain.exceptions.ActorNoEncontradaException;
import org.example.peliculas.domain.exceptions.ActorYaAsignadoException;
import org.example.peliculas.domain.exceptions.PeliculaDuplicadaException;
import org.example.peliculas.domain.exceptions.PeliculaNoEncontradaException;
import org.example.peliculas.domain.fake.InMemoryActorRepository;
import org.example.peliculas.domain.fake.InMemoryPeliculaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PeliculaServiceTest {

    private InMemoryPeliculaRepository peliculas;
    private InMemoryActorRepository actores;
    private PeliculaService service;

    @BeforeEach
    void setUp() {
        peliculas = new InMemoryPeliculaRepository();
        actores = new InMemoryActorRepository();
        service = new PeliculaService(peliculas, actores);
    }

    private Pelicula pelicula(String titulo, Integer anio) {
        return new Pelicula(null, titulo, anio, "Director", Genero.DRAMA, 120, "Sinopsis");
    }

    @Test
    void listar_devuelveTodasLasPeliculas() {
        peliculas.insert(pelicula("Uno", 2000));
        peliculas.insert(pelicula("Dos", 2001));

        List<Pelicula> resultado = service.listar();

        assertEquals(2, resultado.size());
    }

    @Test
    void obtener_existente_devuelvePelicula() {
        Pelicula creada = peliculas.insert(pelicula("Uno", 2000));

        Pelicula resultado = service.obtener(creada.getId());

        assertEquals("Uno", resultado.getTitulo());
    }

    @Test
    void obtener_inexistente_lanzaPeliculaNoEncontrada() {
        assertThrows(PeliculaNoEncontradaException.class, () -> service.obtener(99L));
    }

    @Test
    void crear_nueva_asignaId() {
        Pelicula resultado = service.crear(pelicula("Nueva", 2010));

        assertTrue(resultado.getId() != null);
        assertEquals("Nueva", resultado.getTitulo());
    }

    @Test
    void crear_duplicada_lanzaPeliculaDuplicada() {
        service.crear(pelicula("Repetida", 2010));

        assertThrows(PeliculaDuplicadaException.class, () -> service.crear(pelicula("Repetida", 2010)));
    }

    @Test
    void actualizar_existente_actualizaDatos() {
        Pelicula creada = service.crear(pelicula("Original", 2000));
        Pelicula cambios = pelicula("Cambiada", 2005);

        Pelicula resultado = service.actualizar(creada.getId(), cambios);

        assertEquals("Cambiada", resultado.getTitulo());
        assertEquals(2005, resultado.getAnio());
    }

    @Test
    void actualizar_inexistente_lanzaPeliculaNoEncontrada() {
        assertThrows(PeliculaNoEncontradaException.class,
                () -> service.actualizar(99L, pelicula("X", 2000)));
    }

    @Test
    void actualizar_aDuplicada_lanzaPeliculaDuplicada() {
        service.crear(pelicula("Otra", 1990));
        Pelicula creada = service.crear(pelicula("Original", 2000));

        assertThrows(PeliculaDuplicadaException.class,
                () -> service.actualizar(creada.getId(), pelicula("Otra", 1990)));
    }

    @Test
    void eliminar_existente_borraPelicula() {
        Pelicula creada = service.crear(pelicula("Borrar", 2000));

        service.eliminar(creada.getId());

        assertTrue(peliculas.findById(creada.getId()).isEmpty());
    }

    @Test
    void eliminar_inexistente_lanzaPeliculaNoEncontrada() {
        assertThrows(PeliculaNoEncontradaException.class, () -> service.eliminar(99L));
    }

    @Test
    void asignarActor_valido_asignaActor() {
        Pelicula creada = service.crear(pelicula("Con actor", 2000));
        Actor actor = actores.insert(new Actor(null, "Actor", "España", 1980));

        service.asignarActor(creada.getId(), actor.getId());

        assertTrue(peliculas.existeActorAsignado(creada.getId(), actor.getId()));
    }

    @Test
    void asignarActor_peliculaInexistente_lanzaPeliculaNoEncontrada() {
        Actor actor = actores.insert(new Actor(null, "Actor", "España", 1980));

        assertThrows(PeliculaNoEncontradaException.class,
                () -> service.asignarActor(99L, actor.getId()));
    }

    @Test
    void asignarActor_actorInexistente_lanzaActorNoEncontrada() {
        Pelicula creada = service.crear(pelicula("Con actor", 2000));

        assertThrows(ActorNoEncontradaException.class,
                () -> service.asignarActor(creada.getId(), 99L));
    }

    @Test
    void asignarActor_yaAsignado_lanzaActorYaAsignado() {
        Pelicula creada = service.crear(pelicula("Con actor", 2000));
        Actor actor = actores.insert(new Actor(null, "Actor", "España", 1980));
        service.asignarActor(creada.getId(), actor.getId());

        assertThrows(ActorYaAsignadoException.class,
                () -> service.asignarActor(creada.getId(), actor.getId()));
    }
}
