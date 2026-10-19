package org.example.peliculas.controllers;

import jakarta.validation.Valid;
import java.util.List;
import org.example.peliculas.controllers.dto.ActorRequest;
import org.example.peliculas.controllers.dto.ActorResponse;
import org.example.peliculas.controllers.mapper.ActorMapper;
import org.example.peliculas.domain.Actor;
import org.example.peliculas.domain.service.ActorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actores")
public class ActorController {

    private final ActorService actorService;
    private final ActorMapper actorMapper;

    public ActorController(ActorService actorService, ActorMapper actorMapper) {
        this.actorService = actorService;
        this.actorMapper = actorMapper;
    }

    @GetMapping
    public List<ActorResponse> listar() {
        return actorMapper.toResponseList(actorService.listar());
    }

    @GetMapping("/{id}")
    public ActorResponse obtener(@PathVariable Long id) {
        return actorMapper.toResponse(actorService.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActorResponse crear(@Valid @RequestBody ActorRequest request) {
        Actor creado = actorService.crear(actorMapper.toDomain(request));
        return actorMapper.toResponse(creado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        actorService.eliminar(id);
    }
}
