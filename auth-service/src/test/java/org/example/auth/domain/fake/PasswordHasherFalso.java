package org.example.auth.domain.fake;

import org.example.auth.domain.repository.PasswordHasher;

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
