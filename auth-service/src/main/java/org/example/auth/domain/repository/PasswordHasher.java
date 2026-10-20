package org.example.auth.domain.repository;

public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String hash);
}
