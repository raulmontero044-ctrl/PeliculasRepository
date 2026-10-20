package org.example.peliculas.soporte;

import java.util.UUID;
import org.example.peliculas.domain.ParTokens;
import org.example.peliculas.domain.service.AuthService;

public final class TokensPrueba {

    private static final String PASSWORD = "secret123";

    private TokensPrueba() {
    }

    public static String registrarYLogin(AuthService authService) {
        String username = "usuario-" + UUID.randomUUID();
        authService.register(username, PASSWORD);
        ParTokens par = authService.login(username, PASSWORD);
        return par.accessToken().getToken();
    }

    public static String bearer(AuthService authService) {
        return "Bearer " + registrarYLogin(authService);
    }
}
