package org.example.peliculas.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.peliculas.domain.service.AuthService;
import org.example.peliculas.soporte.TokensPrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ActorControllerTest {

    private static final String ACTOR_VALIDO = """
            {
              "nombre": "Actor Nuevo",
              "pais": "Mexico",
              "anioNacimiento": 1970
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    private String auth;

    @BeforeEach
    void autenticar() {
        auth = TokensPrueba.bearer(authService);
    }

    @Test
    void listar_devuelveTodosLosActores() throws Exception {
        mockMvc.perform(get("/api/actores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void obtener_existente_devuelveActor() throws Exception {
        mockMvc.perform(get("/api/actores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tom Hanks"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/actores/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_valido_devuelve201() throws Exception {
        mockMvc.perform(post("/api/actores")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ACTOR_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Actor Nuevo"));
    }

    @Test
    void crear_invalido_devuelve400() throws Exception {
        String invalido = ACTOR_VALIDO.replace("1970", "1500").replace("Mexico", "");

        mockMvc.perform(post("/api/actores")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    void eliminar_sinPeliculas_devuelve204() throws Exception {
        String creado = mockMvc.perform(post("/api/actores")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ACTOR_VALIDO))
                .andReturn().getResponse().getContentAsString();
        String id = creado.replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");

        mockMvc.perform(delete("/api/actores/" + id).header("Authorization", auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_conPeliculas_devuelve409() throws Exception {
        mockMvc.perform(delete("/api/actores/1").header("Authorization", auth))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void eliminar_inexistente_devuelve404() throws Exception {
        mockMvc.perform(delete("/api/actores/999").header("Authorization", auth))
                .andExpect(status().isNotFound());
    }
}
