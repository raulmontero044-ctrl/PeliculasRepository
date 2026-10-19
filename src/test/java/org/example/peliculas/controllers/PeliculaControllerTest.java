package org.example.peliculas.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class PeliculaControllerTest {

    private static final String PELICULA_VALIDA = """
            {
              "titulo": "Pelicula Nueva",
              "anio": 2020,
              "director": "Directora",
              "genero": "DRAMA",
              "duracionMinutos": 120,
              "sinopsis": "Una sinopsis"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listar_devuelveTodasLasPeliculas() throws Exception {
        mockMvc.perform(get("/api/peliculas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void obtener_existente_devuelvePeliculaConActores() throws Exception {
        mockMvc.perform(get("/api/peliculas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Forrest Gump"))
                .andExpect(jsonPath("$.actores", hasSize(1)));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/peliculas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void obtener_idNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/peliculas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void crear_valida_devuelve201() throws Exception {
        mockMvc.perform(post("/api/peliculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Pelicula Nueva"));
    }

    @Test
    void crear_duplicada_devuelve409() throws Exception {
        String duplicada = """
                {
                  "titulo": "Forrest Gump",
                  "anio": 1994,
                  "director": "Robert Zemeckis",
                  "genero": "DRAMA",
                  "duracionMinutos": 142,
                  "sinopsis": "Duplicada"
                }
                """;

        mockMvc.perform(post("/api/peliculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicada))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void crear_invalida_devuelve400ConErrores() throws Exception {
        String invalida = """
                {
                  "titulo": "",
                  "anio": 1500,
                  "director": "",
                  "genero": "DRAMA",
                  "duracionMinutos": 0,
                  "sinopsis": "x"
                }
                """;

        mockMvc.perform(post("/api/peliculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    void crear_jsonMalformado_devuelve400() throws Exception {
        mockMvc.perform(post("/api/peliculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void crear_generoInvalido_devuelve400() throws Exception {
        String invalida = PELICULA_VALIDA.replace("DRAMA", "NO_EXISTE");

        mockMvc.perform(post("/api/peliculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalida))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_valida_devuelveOk() throws Exception {
        String cambio = PELICULA_VALIDA.replace("Pelicula Nueva", "Forrest Gump v2").replace("2020", "1994");

        mockMvc.perform(put("/api/peliculas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Forrest Gump v2"));
    }

    @Test
    void actualizar_inexistente_devuelve404() throws Exception {
        mockMvc.perform(put("/api/peliculas/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_VALIDA))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_aDuplicada_devuelve409() throws Exception {
        String duplicada = """
                {
                  "titulo": "El diablo viste de Prada",
                  "anio": 2006,
                  "director": "David Frankel",
                  "genero": "COMEDIA",
                  "duracionMinutos": 109,
                  "sinopsis": "Duplicada"
                }
                """;

        mockMvc.perform(put("/api/peliculas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicada))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/peliculas/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/peliculas/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_inexistente_devuelve404() throws Exception {
        mockMvc.perform(delete("/api/peliculas/999"))
                .andExpect(status().isNotFound());
    }
}
