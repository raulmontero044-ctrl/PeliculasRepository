package org.example.peliculas.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AsignacionActoresTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void asignar_actor_valido_devuelve201ConLaPelicula() throws Exception {
        mockMvc.perform(post("/api/peliculas/1/actores/2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.actores").isArray());
    }

    @Test
    void asignar_actor_yaAsignado_devuelve409() throws Exception {
        mockMvc.perform(post("/api/peliculas/1/actores/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void asignar_actor_inexistente_devuelve404() throws Exception {
        mockMvc.perform(post("/api/peliculas/1/actores/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void asignar_a_pelicula_inexistente_devuelve404() throws Exception {
        mockMvc.perform(post("/api/peliculas/999/actores/1"))
                .andExpect(status().isNotFound());
    }
}
