package org.example.peliculas.controllers.mapper;

import java.util.List;
import org.example.peliculas.controllers.dto.ActorRequest;
import org.example.peliculas.controllers.dto.ActorResponse;
import org.example.peliculas.domain.Actor;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActorMapper {

    Actor toDomain(ActorRequest request);

    ActorResponse toResponse(Actor actor);

    List<ActorResponse> toResponseList(List<Actor> actores);
}
