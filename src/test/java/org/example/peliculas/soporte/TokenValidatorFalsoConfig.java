package org.example.peliculas.soporte;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TokenValidatorFalsoConfig {

    @Bean
    @Primary
    public TokenValidatorFalso tokenValidatorFalso() {
        return new TokenValidatorFalso();
    }
}
