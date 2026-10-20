package org.example.peliculas.controllers.auth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;
import org.example.peliculas.domain.ParTokens;
import org.example.peliculas.domain.repository.AuthTokenRepository;
import org.example.peliculas.domain.service.AuthService;
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
    private AuthService authService;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private String username;
    private final String password = "secret123";
    private String tokenValido;

    @BeforeEach
    void autenticar() {
        username = "usuario-" + UUID.randomUUID();
        authService.register(username, password);
        tokenValido = authService.login(username, password).accessToken().getToken();
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
        ParTokens par = obtenerParDeTokens();

        mockMvc.perform(post("/api/peliculas")
                        .header("Authorization", "Bearer " + par.refreshToken().getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PELICULA_NUEVA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token inválido"));
    }

    @Test
    void post_conAccessTokenExpirado_devuelve401() throws Exception {
        ParTokens par = obtenerParDeTokens();
        var expirado = par.accessToken();
        expirado.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        authTokenRepository.save(expirado);

        mockMvc.perform(post("/api/peliculas")
                        .header("Authorization", "Bearer " + expirado.getToken())
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
    void lasRutasDeAuthNoSeFiltran() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "sin-sesion",
                                  "password": "cualquiera"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario no existe"));
    }

    private ParTokens obtenerParDeTokens() {
        return authService.login(username, password);
    }
}
