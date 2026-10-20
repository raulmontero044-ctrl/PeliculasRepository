package org.example.peliculas.domain.repository;

public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String hash);
}
