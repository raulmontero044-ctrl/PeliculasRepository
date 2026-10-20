package org.example.peliculas.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.peliculas.soporte.TokenValidatorFalso;
import org.example.peliculas.soporte.TokenValidatorFalsoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TokenValidatorFalsoConfig.class)
@Transactional
class AsignacionActoresTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenValidatorFalso tokenValidator;

    private String auth;

    @BeforeEach
    void autenticar() {
        auth = "Bearer " + tokenValidator.emitir("usuario-prueba");
    }

    @Test
    void asignar_actor_valido_devuelve201ConLaPelicula() throws Exception {
        mockMvc.perform(post("/api/peliculas/1/actores/2")
                        .header("Authorization", auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.actores").isArray());
    }

    @Test
    void asignar_actor_yaAsignado_devuelve409() throws Exception {
        mockMvc.perform(post("/api/peliculas/1/actores/1")
                        .header("Authorization", auth))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void asignar_actor_inexistente_devuelve404() throws Exception {
        mockMvc.perform(post("/api/peliculas/1/actores/999")
                        .header("Authorization", auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void asignar_a_pelicula_inexistente_devuelve404() throws Exception {
        mockMvc.perform(post("/api/peliculas/999/actores/1")
                        .header("Authorization", auth))
                .andExpect(status().isNotFound());
    }
}
