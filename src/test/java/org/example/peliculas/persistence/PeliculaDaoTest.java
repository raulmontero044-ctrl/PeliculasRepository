package org.example.peliculas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.example.peliculas.domain.Genero;
import org.example.peliculas.domain.Pelicula;
import org.example.peliculas.domain.repository.PeliculaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PeliculaDaoTest {

    @Autowired
    private PeliculaRepository peliculaRepository;

    private Pelicula nueva(String titulo, Integer anio) {
        return new Pelicula(null, titulo, anio, "Director", Genero.DRAMA, 120, "Sinopsis");
    }

    @Test
    void findAll_devuelveLosDatosSemilla() {
        List<Pelicula> peliculas = peliculaRepository.findAll();

        assertEquals(4, peliculas.size());
    }

    @Test
    void findById_devuelvePeliculaConSusActores() {
        Pelicula pelicula = peliculaRepository.findById(1L).orElseThrow();

        assertEquals("Forrest Gump", pelicula.getTitulo());
        assertEquals(1, pelicula.getActores().size());
    }

    @Test
    void findById_inexistente_estaVacio() {
        assertTrue(peliculaRepository.findById(999L).isEmpty());
    }

    @Test
    void insert_persisteYPersisteElId() {
        Pelicula guardada = peliculaRepository.insert(nueva("Nueva Persistida", 2021));

        assertTrue(guardada.getId() != null);
        Optional<Pelicula> recuperada = peliculaRepository.findById(guardada.getId());
        assertTrue(recuperada.isPresent());
        assertEquals("Nueva Persistida", recuperada.get().getTitulo());
    }

    @Test
    void update_modificaLaPelicula() {
        Pelicula pelicula = peliculaRepository.findById(1L).orElseThrow();
        pelicula.setTitulo("Forrest Gump (remaster)");

        peliculaRepository.update(pelicula);

        assertEquals("Forrest Gump (remaster)",
                peliculaRepository.findById(1L).orElseThrow().getTitulo());
    }

    @Test
    void findByTituloAndAnio_encuentraExistente() {
        assertTrue(peliculaRepository.findByTituloAndAnio("Forrest Gump", 1994).isPresent());
    }

    @Test
    void deleteById_eliminaPeliculaYSusRelaciones() {
        Pelicula creada = peliculaRepository.insert(nueva("Para Borrar", 2019));

        peliculaRepository.deleteById(creada.getId());

        assertTrue(peliculaRepository.findById(creada.getId()).isEmpty());
    }

    @Test
    void asignarActor_yExisteActorAsignado() {
        Long peliculaId = peliculaRepository.insert(nueva("Con Actor", 2018)).getId();
        Long actorId = 2L;

        assertFalse(peliculaRepository.existeActorAsignado(peliculaId, actorId));
        peliculaRepository.asignarActor(peliculaId, actorId);
        assertTrue(peliculaRepository.existeActorAsignado(peliculaId, actorId));
    }

    @Test
    void quitarActor_eliminaLaRelacion() {
        peliculaRepository.quitarActor(1L, 1L);

        assertFalse(peliculaRepository.existeActorAsignado(1L, 1L));
    }
}
