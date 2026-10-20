package org.example.peliculas.controllers.config;

import org.example.peliculas.controllers.auth.TokenAuthenticationFilter;
import org.example.peliculas.domain.service.AuthService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class AuthConfig {

    @Bean
    public FilterRegistrationBean<TokenAuthenticationFilter> tokenAuthenticationFilter(
            AuthService authService, ObjectMapper objectMapper) {
        TokenAuthenticationFilter filtro = new TokenAuthenticationFilter(authService, objectMapper);
        FilterRegistrationBean<TokenAuthenticationFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.addUrlPatterns("/*");
        registro.setName("tokenAuthenticationFilter");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}
