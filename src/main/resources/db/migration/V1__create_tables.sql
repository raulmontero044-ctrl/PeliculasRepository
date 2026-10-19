CREATE TABLE actores (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(150) NOT NULL,
    pais             VARCHAR(100) NOT NULL,
    anio_nacimiento  INT          NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE peliculas (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    titulo           VARCHAR(200) NOT NULL,
    anio             INT          NOT NULL,
    director         VARCHAR(150) NOT NULL,
    genero           VARCHAR(30)  NOT NULL,
    duracion_minutos INT          NOT NULL,
    sinopsis         VARCHAR(2000),
    PRIMARY KEY (id),
    CONSTRAINT uq_pelicula_titulo_anio UNIQUE (titulo, anio)
);

CREATE TABLE pelicula_actor (
    pelicula_id BIGINT NOT NULL,
    actor_id    BIGINT NOT NULL,
    PRIMARY KEY (pelicula_id, actor_id),
    CONSTRAINT fk_pelicula_actor_pelicula FOREIGN KEY (pelicula_id)
        REFERENCES peliculas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pelicula_actor_actor FOREIGN KEY (actor_id)
        REFERENCES actores (id) ON DELETE RESTRICT
);
