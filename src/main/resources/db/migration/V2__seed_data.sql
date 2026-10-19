INSERT INTO actores (id, nombre, pais, anio_nacimiento) VALUES
    (1, 'Tom Hanks', 'Estados Unidos', 1956),
    (2, 'Meryl Streep', 'Estados Unidos', 1949),
    (3, 'Penelope Cruz', 'Espana', 1974),
    (4, 'Ricardo Darin', 'Argentina', 1957);

INSERT INTO peliculas (id, titulo, anio, director, genero, duracion_minutos, sinopsis) VALUES
    (1, 'Forrest Gump', 1994, 'Robert Zemeckis', 'DRAMA', 142,
        'La vida de un hombre con buen corazon que presencia eventos historicos.'),
    (2, 'El diablo viste de Prada', 2006, 'David Frankel', 'COMEDIA', 109,
        'Una joven periodista trabaja para una temible editora de moda.'),
    (3, 'La vida es bella', 1997, 'Roberto Benigni', 'DRAMA', 116,
        'Un padre protege a su hijo en un campo de concentracion.'),
    (4, 'Vicky Cristina Barcelona', 2008, 'Woody Allen', 'COMEDIA', 96,
        'Dos amigas estadounidenses viven un verano en Barcelona.');

INSERT INTO pelicula_actor (pelicula_id, actor_id) VALUES
    (1, 1),
    (2, 2),
    (3, 4),
    (4, 3);
