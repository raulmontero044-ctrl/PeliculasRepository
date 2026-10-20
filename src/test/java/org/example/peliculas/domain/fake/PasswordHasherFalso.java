package org.example.peliculas.domain.fake;

import org.example.peliculas.domain.repository.PasswordHasher;

public class PasswordHasherFalso implements PasswordHasher {

    @Override
    public String hash(String password) {
        return "hash:" + password;
    }

    @Override
    public boolean matches(String password, String hash) {
        return this.hash(password).equals(hash);
    }
}
