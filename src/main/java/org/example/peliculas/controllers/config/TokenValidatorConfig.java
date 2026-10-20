package org.example.peliculas.controllers.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.example.peliculas.adapters.HttpTokenValidator;
import org.example.peliculas.domain.repository.TokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class TokenValidatorConfig {

    @Bean
    public TokenValidator tokenValidator(ObjectMapper objectMapper,
                                         @Value("${auth.service.url}") String authServiceUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        RestClient restClient = RestClient.builder()
                .baseUrl(authServiceUrl)
                .requestFactory(requestFactory)
                .build();
        return new HttpTokenValidator(restClient, objectMapper);
    }
}
