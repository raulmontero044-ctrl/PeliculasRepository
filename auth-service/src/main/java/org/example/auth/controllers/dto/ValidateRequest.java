package org.example.auth.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidateRequest {

    @NotBlank(message = "no debe estar vacío")
    private String token;

    public ValidateRequest() {
    }

    public ValidateRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
