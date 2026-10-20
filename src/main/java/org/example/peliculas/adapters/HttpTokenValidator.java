package org.example.peliculas.adapters;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.example.peliculas.domain.exceptions.AuthNoDisponibleException;
import org.example.peliculas.domain.exceptions.TokenExpiradoException;
import org.example.peliculas.domain.exceptions.TokenInvalidoException;
import org.example.peliculas.domain.repository.TokenValidator;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

public class HttpTokenValidator implements TokenValidator {

    private static final String MENSAJE_NO_DISPONIBLE = "Servicio de autenticación no disponible";
    private static final String MOTIVO_EXPIRADO = "TOKEN_EXPIRADO";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public HttpTokenValidator(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void validar(String accessToken) {
        try {
            restClient.post()
                    .uri("/api/auth/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("token", accessToken))
                    .exchange((request, response) -> {
                        if (response.getStatusCode().value() == 200) {
                            return null;
                        }
                        String cuerpo = response.getBody() == null
                                ? "" : new String(response.getBody().readAllBytes(),
                                        StandardCharsets.UTF_8);
                        if (response.getStatusCode().value() == 401) {
                            lanzarErrorDeToken(cuerpo);
                        }
                        throw new AuthNoDisponibleException(MENSAJE_NO_DISPONIBLE);
                    });
        } catch (AuthNoDisponibleException | TokenInvalidoException | TokenExpiradoException ex) {
            throw ex;
        } catch (RestClientException ex) {
            Throwable causa = ex.getCause();
            if (causa instanceof AuthNoDisponibleException
                    || causa instanceof TokenInvalidoException
                    || causa instanceof TokenExpiradoException) {
                throw (RuntimeException) causa;
            }
            throw new AuthNoDisponibleException(MENSAJE_NO_DISPONIBLE, ex);
        }
    }

    private void lanzarErrorDeToken(String cuerpo) {
        Map<?, ?> mapa;
        try {
            mapa = objectMapper.readValue(cuerpo, Map.class);
        } catch (RuntimeException ex) {
            throw new AuthNoDisponibleException(MENSAJE_NO_DISPONIBLE, ex);
        }
        Object motivo = mapa.get("motivo");
        Object mensaje = mapa.get("mensaje");
        String texto = mensaje != null ? mensaje.toString() : "Token inválido";
        if (MOTIVO_EXPIRADO.equals(String.valueOf(motivo))) {
            throw new TokenExpiradoException(texto);
        }
        throw new TokenInvalidoException(texto);
    }
}
