package org.example.peliculas.controllers.auth;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.peliculas.soporte.TokenValidatorFalso;
import org.example.peliculas.soporte.TokenValidatorFalsoConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TokenValidatorFalsoConfig.class)
@Transactional
class TokenAuthenticationFilterTest {

    private static final String PELICULA_NUEVA = """
            {
              "titulo": "Pelicula Con Token",
              "anio": 2021,
              "director": "Directora",
              "genero": "DRAMA",
              "duracionMinutos": 100,
              "sinopsis": "Sinopsis"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenValidatorFalso tokenValidator;

    private String tokenValido;

    @BeforeEach
    void emitirToken() {
        tokenValido = tokenValidator.emitir("raul");
    }

    @AfterEach
    void reactivarServicio() {
        tokenValidator.reactivar();
    }

    @Test
    void get_siguePublicoSinToken() throws Exception {
        mockMvc.perform(get("/api/peliculas"))
                .andExpect(status().isOk());
    }

    @Test
    void getDeActores_siguePublicoSinToken() throws Exception {
        mockMvc.perform(get("/api/actores/1"))
                .andExpect(status().isOk());
    }

    @Test
    void post_sinToken_devuelve401ConElFormatoDeErrorRespuesta() throws Exception {
        mockMvc.perform(post("/api/peliculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").value("Falta la cabecera Authorization"))
                .andExpect(jsonPath("$.path").value("/api/peliculas"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void post_conAccessTokenValido_devuelve201() throws Exception {
        mockMvc.perform(post("/api/peliculas")
                        .header("Authorization", "Bearer " + tokenValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Pelicula Con Token"));
    }

    @Test
    void put_sinToken_devuelve401() throws Exception {
        mockMvc.perform(put("/api/peliculas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_sinToken_devuelve401() throws Exception {
        mockMvc.perform(delete("/api/peliculas/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void post_conCabeceraMalformada_devuelve401() throws Exception {
        mockMvc.perform(post("/api/peliculas")
                        .header("Authorization", "Token " + tokenValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", containsString("Bearer")));
    }

    @Test
    void post_conRefreshToken_devuelve401() throws Exception {
        String refresh = tokenValidator.emitirRefresh("raul");

        mockMvc.perform(post("/api/peliculas")
                        .header("Authorization", "Bearer " + refresh)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token inválido"));
    }

    @Test
    void post_conAccessTokenExpirado_devuelve401() throws Exception {
        String expirado = tokenValidator.emitirExpirado("raul");

        mockMvc.perform(post("/api/peliculas")
                        .header("Authorization", "Bearer " + expirado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token expirado"));
    }

    @Test
    void post_conTokenInexistente_devuelve401() throws Exception {
        mockMvc.perform(post("/api/peliculas")
                        .header("Authorization", "Bearer " + "abcdef0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token inválido"));
    }

    @Test
    void post_conServicioDeAuthCaido_devuelve503() throws Exception {
        tokenValidator.caer();

        mockMvc.perform(post("/api/peliculas")
                        .header("Authorization", "Bearer " + tokenValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.mensaje")
                        .value(TokenValidatorFalso.MENSAJE_NO_DISPONIBLE));
    }

    @Test
    void postAUnaRutaNoProtegida_noDevuelve401() throws Exception {
        var resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "sin-sesion",
                                  "password": "cualquiera"
                                }
                                """))
                .andReturn();

        assertNotEquals(401, resultado.getResponse().getStatus());
    }
}
