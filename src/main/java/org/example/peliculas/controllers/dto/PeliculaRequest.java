package org.example.peliculas.controllers.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.peliculas.domain.Genero;

public class PeliculaRequest {

    @NotBlank(message = "no debe estar vacío")
    @Size(max = 200, message = "no debe superar 200 caracteres")
    private String titulo;

    @NotNull(message = "no debe ser nulo")
    @Min(value = 1888, message = "debe ser mayor o igual a 1888")
    @Max(value = 2100, message = "debe ser menor o igual a 2100")
    private Integer anio;

    @NotBlank(message = "no debe estar vacío")
    @Size(max = 150, message = "no debe superar 150 caracteres")
    private String director;

    @NotNull(message = "no debe ser nulo")
    private Genero genero;

    @NotNull(message = "no debe ser nulo")
    @Positive(message = "debe ser mayor que 0")
    private Integer duracionMinutos;

    @Size(max = 2000, message = "no debe superar 2000 caracteres")
    private String sinopsis;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }
}
