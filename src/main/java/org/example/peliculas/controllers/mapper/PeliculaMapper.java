package org.example.peliculas.controllers.mapper;

import java.util.List;
import org.example.peliculas.controllers.dto.PeliculaRequest;
import org.example.peliculas.controllers.dto.PeliculaResponse;
import org.example.peliculas.domain.Pelicula;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = ActorMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PeliculaMapper {

    Pelicula toDomain(PeliculaRequest request);

    PeliculaResponse toResponse(Pelicula pelicula);

    List<PeliculaResponse> toResponseList(List<Pelicula> peliculas);
}
