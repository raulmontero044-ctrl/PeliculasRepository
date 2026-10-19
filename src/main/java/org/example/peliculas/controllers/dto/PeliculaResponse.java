package org.example.peliculas.controllers.dto;

import java.util.List;
import org.example.peliculas.domain.Genero;

public class PeliculaResponse {

    private Long id;
    private String titulo;
    private Integer anio;
    private String director;
    private Genero genero;
    private Integer duracionMinutos;
    private String sinopsis;
    private List<ActorResponse> actores;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<ActorResponse> getActores() {
        return actores;
    }

    public void setActores(List<ActorResponse> actores) {
        this.actores = actores;
    }
}
