package org.example.peliculas.controllers.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ActorRequest {

    @NotBlank(message = "no debe estar vacío")
    @Size(max = 150, message = "no debe superar 150 caracteres")
    private String nombre;

    @NotBlank(message = "no debe estar vacío")
    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String pais;

    @NotNull(message = "no debe ser nulo")
    @Min(value = 1850, message = "debe ser mayor o igual a 1850")
    @Max(value = 2100, message = "debe ser menor o igual a 2100")
    private Integer anioNacimiento;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Integer getAnioNacimiento() {
        return anioNacimiento;
    }

    public void setAnioNacimiento(Integer anioNacimiento) {
        this.anioNacimiento = anioNacimiento;
    }
}
