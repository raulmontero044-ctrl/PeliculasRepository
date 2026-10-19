package org.example.peliculas.domain;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Pelicula {

    private Long id;
    private String titulo;
    private Integer anio;
    private String director;
    private Genero genero;
    private Integer duracionMinutos;
    private String sinopsis;
    private Set<Actor> actores = new LinkedHashSet<>();

    public Pelicula() {
    }

    public Pelicula(Long id, String titulo, Integer anio, String director, Genero genero,
                    Integer duracionMinutos, String sinopsis) {
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.director = director;
        this.genero = genero;
        this.duracionMinutos = duracionMinutos;
        this.sinopsis = sinopsis;
    }

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

    public Set<Actor> getActores() {
        return actores;
    }

    public void setActores(Set<Actor> actores) {
        this.actores = actores;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pelicula pelicula)) {
            return false;
        }
        return id != null && id.equals(pelicula.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
