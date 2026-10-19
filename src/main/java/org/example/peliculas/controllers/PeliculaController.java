package org.example.peliculas.controllers;

import jakarta.validation.Valid;
import java.util.List;
import org.example.peliculas.controllers.dto.PeliculaRequest;
import org.example.peliculas.controllers.dto.PeliculaResponse;
import org.example.peliculas.controllers.mapper.PeliculaMapper;
import org.example.peliculas.domain.Pelicula;
import org.example.peliculas.domain.service.PeliculaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/peliculas")
public class PeliculaController {

    private final PeliculaService peliculaService;
    private final PeliculaMapper peliculaMapper;

    public PeliculaController(PeliculaService peliculaService, PeliculaMapper peliculaMapper) {
        this.peliculaService = peliculaService;
        this.peliculaMapper = peliculaMapper;
    }

    @GetMapping
    public List<PeliculaResponse> listar() {
        return peliculaMapper.toResponseList(peliculaService.listar());
    }

    @GetMapping("/{id}")
    public PeliculaResponse obtener(@PathVariable Long id) {
        return peliculaMapper.toResponse(peliculaService.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeliculaResponse crear(@Valid @RequestBody PeliculaRequest request) {
        Pelicula creada = peliculaService.crear(peliculaMapper.toDomain(request));
        return peliculaMapper.toResponse(creada);
    }

    @PutMapping("/{id}")
    public PeliculaResponse actualizar(@PathVariable Long id, @Valid @RequestBody PeliculaRequest request) {
        Pelicula actualizada = peliculaService.actualizar(id, peliculaMapper.toDomain(request));
        return peliculaMapper.toResponse(actualizada);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        peliculaService.eliminar(id);
    }

    @PostMapping("/{peliculaId}/actores/{actorId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PeliculaResponse asignarActor(@PathVariable Long peliculaId, @PathVariable Long actorId) {
        return peliculaMapper.toResponse(peliculaService.asignarActor(peliculaId, actorId));
    }
}
