package org.example.peliculas.controllers.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.example.peliculas.controllers.dto.ErrorRespuesta;
import org.example.peliculas.domain.exceptions.AuthNoDisponibleException;
import org.example.peliculas.domain.exceptions.AutenticacionException;
import org.example.peliculas.domain.exceptions.TokenInvalidoException;
import org.example.peliculas.domain.repository.TokenValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_DE_ESCRITURA = Set.of("POST", "PUT", "DELETE");
    private static final String RAIZ_PELICULAS = "/api/peliculas";
    private static final String RAIZ_ACTORES = "/api/actores";
    private static final String PREFIJO_BEARER = "Bearer";

    private final TokenValidator tokenValidator;
    private final ObjectMapper objectMapper;

    public TokenAuthenticationFilter(TokenValidator tokenValidator, ObjectMapper objectMapper) {
        this.tokenValidator = tokenValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!esEscrituraProtegida(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            tokenValidator.validar(extraerToken(request));
        } catch (AuthNoDisponibleException ex) {
            responder(request, response, HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
            return;
        } catch (AutenticacionException ex) {
            responder(request, response, HttpStatus.UNAUTHORIZED, ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean esEscrituraProtegida(HttpServletRequest request) {
        if (!METODOS_DE_ESCRITURA.contains(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return esLaMismaRuta(uri, RAIZ_PELICULAS) || esLaMismaRuta(uri, RAIZ_ACTORES);
    }

    private boolean esLaMismaRuta(String uri, String raiz) {
        return uri.equals(raiz) || uri.startsWith(raiz + "/");
    }

    private String extraerToken(HttpServletRequest request) {
        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || cabecera.isBlank()) {
            throw new TokenInvalidoException("Falta la cabecera Authorization");
        }
        int posicion = cabecera.indexOf(' ');
        if (posicion < 0 || !PREFIJO_BEARER.equalsIgnoreCase(cabecera.substring(0, posicion).trim())) {
            throw new TokenInvalidoException(
                    "Cabecera Authorization inválida: se espera 'Bearer <token>'");
        }
        String token = cabecera.substring(posicion + 1).trim();
        if (token.isEmpty()) {
            throw new TokenInvalidoException("Falta el token en la cabecera Authorization");
        }
        return token;
    }

    private void responder(HttpServletRequest request, HttpServletResponse response,
                           HttpStatus estado, String mensaje) throws IOException {
        ErrorRespuesta cuerpo = ErrorRespuesta.de(estado, mensaje, request.getRequestURI(), null);
        response.setStatus(estado.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(cuerpo));
    }
}
