package org.example.auth.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "no debe estar vacío")
    @Size(max = 50, message = "no debe superar 50 caracteres")
    private String username;

    @NotBlank(message = "no debe estar vacío")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
