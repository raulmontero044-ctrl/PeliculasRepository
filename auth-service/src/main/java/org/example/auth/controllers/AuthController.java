package org.example.auth.controllers;

import jakarta.validation.Valid;
import java.time.Duration;
import org.example.auth.controllers.dto.AccessTokenResponse;
import org.example.auth.controllers.dto.LoginRequest;
import org.example.auth.controllers.dto.RefreshRequest;
import org.example.auth.controllers.dto.RegisterRequest;
import org.example.auth.controllers.dto.TokenPairResponse;
import org.example.auth.controllers.dto.TokenValidoResponse;
import org.example.auth.controllers.dto.ValidateRequest;
import org.example.auth.domain.AuthToken;
import org.example.auth.domain.ParTokens;
import org.example.auth.domain.Usuario;
import org.example.auth.domain.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String TIPO_BEARER = "Bearer";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.getUsername(), request.getPassword());
    }

    @PostMapping("/login")
    public TokenPairResponse login(@Valid @RequestBody LoginRequest request) {
        ParTokens par = authService.login(request.getUsername(), request.getPassword());
        return new TokenPairResponse(
                par.accessToken().getToken(),
                par.refreshToken().getToken(),
                TIPO_BEARER,
                segundosDeVida(par.accessToken()),
                segundosDeVida(par.refreshToken()));
    }

    @PostMapping("/refresh")
    public AccessTokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        AuthToken accessToken = authService.refresh(request.getRefreshToken());
        return new AccessTokenResponse(accessToken.getToken(), TIPO_BEARER,
                segundosDeVida(accessToken));
    }

    @PostMapping("/validate")
    public TokenValidoResponse validate(@Valid @RequestBody ValidateRequest request) {
        Usuario usuario = authService.validateAccessToken(request.getToken());
        return new TokenValidoResponse(usuario.getId(), usuario.getUsername());
    }

    private long segundosDeVida(AuthToken token) {
        return Duration.between(token.getCreatedAt(), token.getExpiresAt()).getSeconds();
    }
}
