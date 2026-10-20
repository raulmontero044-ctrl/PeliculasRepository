package org.example.peliculas.adapters;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.example.peliculas.domain.exceptions.AuthNoDisponibleException;
import org.example.peliculas.domain.exceptions.TokenExpiradoException;
import org.example.peliculas.domain.exceptions.TokenInvalidoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

class HttpTokenValidatorTest {

    private static final String URL_BASE = "http://auth-servicio";
    private static final String URL_VALIDAR = URL_BASE + "/api/auth/validate";
    private static final String SERVICIO_CAIDO = "Servicio de autenticación no disponible";

    private MockRestServiceServer server;
    private HttpTokenValidator validator;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        validator = new HttpTokenValidator(builder.baseUrl(URL_BASE).build(), new ObjectMapper());
    }

    @AfterEach
    void verificarPeticiones() {
        server.verify();
    }

    @Test
    void validar_tokenValido_noLanzaExcepcion() {
        server.expect(once(), requestTo(URL_VALIDAR))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"userId":1,"username":"raul"}""", MediaType.APPLICATION_JSON));

        assertThatCode(() -> validator.validar("token-valido")).doesNotThrowAnyException();
    }

    @Test
    void validar_tokenInvalido_lanzaTokenInvalidoConElMensajeDelServicio() {
        server.expect(once(), requestTo(URL_VALIDAR))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"status":401,"mensaje":"Token inválido","motivo":"TOKEN_INVALIDO"}"""));

        assertThatThrownBy(() -> validator.validar("token-malo"))
                .isInstanceOf(TokenInvalidoException.class)
                .hasMessage("Token inválido");
    }

    @Test
    void validar_tokenExpirado_lanzaTokenExpirado() {
        server.expect(once(), requestTo(URL_VALIDAR))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"status":401,"mensaje":"Token expirado","motivo":"TOKEN_EXPIRADO"}"""));

        assertThatThrownBy(() -> validator.validar("token-viejo"))
                .isInstanceOf(TokenExpiradoException.class)
                .hasMessage("Token expirado");
    }

    @Test
    void validar_servicioConError500_lanzaAuthNoDisponible() {
        server.expect(once(), requestTo(URL_VALIDAR))
                .andRespond(withServerError());

        assertThatThrownBy(() -> validator.validar("token-cualquiera"))
                .isInstanceOf(AuthNoDisponibleException.class)
                .hasMessage(SERVICIO_CAIDO);
    }

    @Test
    void validar_respuestaDeErrorMalformada_lanzaAuthNoDisponible() {
        server.expect(once(), requestTo(URL_VALIDAR))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("esto-no-es-un-json"));

        assertThatThrownBy(() -> validator.validar("token-raro"))
                .isInstanceOf(AuthNoDisponibleException.class)
                .hasMessage(SERVICIO_CAIDO);
    }
}
