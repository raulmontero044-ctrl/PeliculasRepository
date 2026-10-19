package org.example.peliculas.domain;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Actor {

    private Long id;
    private String nombre;
    private String pais;
    private Integer anioNacimiento;
    private Set<Pelicula> peliculas = new LinkedHashSet<>();

    public Actor() {
    }

    public Actor(Long id, String nombre, String pais, Integer anioNacimiento) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.anioNacimiento = anioNacimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Set<Pelicula> getPeliculas() {
        return peliculas;
    }

    public void setPeliculas(Set<Pelicula> peliculas) {
        this.peliculas = peliculas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Actor actor)) {
            return false;
        }
        return id != null && id.equals(actor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
