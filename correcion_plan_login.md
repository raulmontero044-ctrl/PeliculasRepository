# Corrección del plan de login: servicio de autenticación en contenedor propio

> Corrige/actualiza `plan_login.md`. El sistema de login deja de vivir dentro de la app de
> películas y pasa a ser un microservicio `auth-service` desplegado en **otro contenedor Docker**,
> en la **misma red** que la aplicación principal (red por defecto del `compose.yaml`).

## 1) Motivo del cambio y decisiones tomadas

| Decisión | Elección |
|---|---|
| Ubicación del código | Carpeta `auth-service/` **dentro de este repositorio**, con su propio `pom.xml` y `Dockerfile` (un solo repo para entregar) |
| Validación de tokens por la app principal | **Llamada HTTP** al servicio auth (`POST http://auth:8081/api/auth/validate`). Se mantiene el modelo actual: tokens en BD, invalidación solo por expiración |
| Base de datos | **MariaDB propia** para auth (contenedor `db-auth`, volumen `auth_data`, puerto 3308). La app de películas conserva `db` (3307) sin tablas de auth |

Todo lo demás del `plan_login.md` sigue vigente: mismos requisitos (escrituras protegidas, Bearer
5 min / 1 día, diferenciación de errores en login, refresh sin rotar), mismo diseño de dominio
puro, mismas reglas ArchUnit y mismo formato de errores.

## 2) Arquitectura resultante

```
                 red Docker por defecto del compose
   ┌──────────────┬──────────────────┬─────────────────┬──────────────────┐
   │ app  :8080   │ auth :8081       │ db    :3307     │ db-auth :3308    │
   │ películas    │ login/tokens     │ MariaDB         │ MariaDB          │
   │              │                  │ BD "peliculas"  │ BD "auth"        │
   └──────┬───────┴────────┬─────────┴────────┬────────┴────────┬─────────┘
          │                │                  │                 │
          │  POST/PUT/DELETE protegidos:      │                 │
          └──────► POST http://auth:8081/api/auth/validate ◄────┘
```

| Servicio | Build | Puerto host | BD (Flyway propio) |
|---|---|---|---|
| `app` | `Dockerfile` raíz (sin cambios) | 8080 | `db` → BD `peliculas`, migraciones `V1`/`V2` (datos), **sin** V3/V4 |
| `auth` | **nuevo** `auth-service/Dockerfile` | 8081 | `db-auth` → BD `auth`, migraciones `V1__create_auth_tables.sql` / `V2__seed_usuarios.sql` |
| `db` | `mariadb:11` | 3307 | volumen `mariadb_data` |
| `db-auth` | `mariadb:11` (**nuevo**) | 3308 | volumen `auth_data` (**nuevo**) |

- **Endpoints**: `register`, `login`, `refresh` y `validate` viven **solo en el 8081**. La app del
  8080 **ya no expone `/api/auth/**`**.
- `app` resuelve `http://auth:8081` por nombre de servicio en la red del compose (y por
  `localhost:8081` en desarrollo local).

## 3) Estructura de carpetas

```
PeliculasRepository/
├── compose.yaml                      (db, db-auth, app, auth)
├── Dockerfile                        (app, sin cambios)
├── pom.xml                           (app: se quita spring-security-crypto)
├── src/                              (app de películas, SIN código auth)
├── plan_login.md                     (referencia original)
├── correcion_plan_login.md           (este documento)
└── auth-service/
    ├── pom.xml
    ├── Dockerfile
    ├── .dockerignore
    └── src/
        ├── main/java/org/example/auth/...
        ├── main/resources/application.properties
        ├── main/resources/db/migration/V1__create_auth_tables.sql
        ├── main/resources/db/migration/V2__seed_usuarios.sql
        └── test/java/org/example/auth/...
```

Paquete nuevo: `org.example.auth` (mismo diseño por capas que la app: `domain`, `persistence`,
`controllers`).

## 4) Qué se mueve al `auth-service`

**Código principal** (copiado y reempaquetado de `org.example.peliculas` a `org.example.auth`):

- Dominio: `Usuario`, `AuthToken`, `TokenTipo`, `ParTokens`.
- Excepciones: `AutenticacionException`, `UsuarioYaExisteException`, `UsuarioNoEncontradoException`,
  `CredencialesIncorrectasException`, `TokenInvalidoException`, `TokenExpiradoException`.
- Puertos: `UsuarioRepository`, `AuthTokenRepository`, `PasswordHasher`.
- Servicio: `AuthService` (register/login/refresh/validateAccessToken, 5 min / 1 día).
- Persistencia: `UsuarioDao`, `AuthTokenDao`, `UsuarioRowMapper`, `AuthTokenRowMapper`.
- Web: `AuthController` (register/login/refresh), DTOs (`RegisterRequest`, `LoginRequest`,
  `RefreshRequest`, `TokenPairResponse`, `AccessTokenResponse`), `AuthHandler`, `ErrorRespuesta`,
  `ErrorCampo`, handler de validación, `BCryptPasswordHasher`, `AuthConfig`, `ServiceConfig`
  (BCrypt + `AuthService` + `Clock.systemDefaultZone()`).

**Migraciones** (renumeradas porque es BD/historia de Flyway propia):

| Antes (app) | Ahora (auth-service) |
|---|---|
| `V3__create_auth_tables.sql` | `V1__create_auth_tables.sql` (idéntico) |
| `V4__seed_usuarios.sql` (semilla admin/editor/viewer) | `V2__seed_usuarios.sql` (idéntico) |

**Tests que se mueven**: `AuthServiceTest`, `UsuarioDaoTest`, `AuthTokenDaoTest`,
`AuthControllerTest`, `MigracionesFlywayTest` (ajustado a V1/V2), fakes
`InMemoryUsuarioRepository`/`InMemoryAuthTokenRepository`/`PasswordHasherFalso`.

**Nuevo endpoint de introspección** (contrato consumido por la app principal):

```
POST /api/auth/validate
Body: { "token": "<accessToken>" }

200 OK   → { "userId": 1, "username": "raul" }
401      → ErrorRespuesta + campo extra "motivo": "TOKEN_INVALIDO" | "TOKEN_EXPIRADO"
```

Solo acepta tokens tipo `ACCESS` (un refresh token → 401 `TOKEN_INVALIDO`).

**Nuevo**: `Dockerfile` multi-stage Maven (igual que el raíz: `mvn -B -q clean package -DskipTests`
sobre `maven:3.9-eclipse-temurin-21`, ejecución en `eclipse-temurin:21-jre`, `EXPOSE 8081`,
`server.port=8081`) y `.dockerignore` (`target/`, `.git/`, …).

**Nuevo**: reglas ArchUnit propias (dominio puro, los controladores no dependen de persistencia,
DTOs con sufijo Request/Response), copiadas de `ArquitecturaTest` y ajustadas a `org.example.auth`.

## 5) Qué cambia en la app principal (películas)

### Se elimina

- Todo el código auth de `org.example.peliculas`: dominio (`Usuario`, `AuthToken`, `TokenTipo`,
  `ParTokens`), sus excepciones y puertos, `AuthService`, `UsuarioDao`/`AuthTokenDao` + rowmappers,
  `AuthController` + DTOs de auth, `AuthHandler`, `AuthConfig`, `BCryptPasswordHasher`,
  `V3__create_auth_tables.sql`, `V4__seed_usuarios.sql`.
- Dependencia `spring-security-crypto` del `pom.xml` (el BCrypt viaja en el otro servicio).
- Tests `AuthServiceTest`, `UsuarioDaoTest`, `AuthTokenDaoTest`, `AuthControllerTest`,
  `MigracionesFlywayTest` y helper `TokensPrueba` (se van con el servicio).

### Se añade

| Clase | Ubicación | Propósito |
|---|---|---|
| `TokenValidator` | `domain/repository/` (puerto puro) | `Usuario validar(String accessToken)` → lanza `TokenInvalidoException` / `TokenExpiradoException` / `AuthNoDisponibleException` |
| `AuthNoDisponibleException` | `domain/exceptions/` | El servicio auth no responde o devuelve 5xx |
| `HttpTokenValidator` | nuevo paquete `adapters/` (fuera de `domain` y de `controllers`) | Adaptador `RestClient` que llama a `POST ${auth.service.url}/api/auth/validate` y traduce `motivo` → excepción |
| `TokenValidatorConfig` | `controllers/config/` | Bean `TokenValidator` con `RestClient.Builder` y `@Value("${auth.service.url}")` |

### Filtro reescrito

`TokenAuthenticationFilter` conserva su alcance actual (solo `POST`/`PUT`/`DELETE` de
`/api/peliculas` y `/api/actores`, `Authorization: Bearer <token>`), pero ya **no toca BD**: extrae
el token y delega en `TokenValidator`.

- Sin cabecera o mal formada → **401** (escrito por el propio filtro, formato `ErrorRespuesta`).
- `TokenExpiradoException` → **401** con el mensaje devuelto por el servicio auth ("Token expirado").
- `TokenInvalidoException` → **401** con su mensaje.
- `AuthNoDisponibleException` → **503** "Servicio de autenticación no disponible" (nuevo código).

### Configuración

- `application.properties`: `auth.service.url=http://localhost:8081` (en compose se pisa con
  `AUTH_SERVICE_URL=http://auth:8081`).
- `ServiceConfig` deja de crear `AuthService` y BCrypt.

## 6) Estrategia de tests tras la separación

| Test | Situación nueva |
|---|---|
| `HttpTokenValidatorTest` (**nuevo**) | `MockRestServiceServer`: 200 → devuelve usuario; 401 `TOKEN_EXPIRADO` → `TokenExpiradoException`; 401 `TOKEN_INVALIDO` → `TokenInvalidoException`; timeout/conexión → `AuthNoDisponibleException` |
| `TokenAuthenticationFilterTest` (reescrito) | Bean `@Primary` **fake en memoria** de `TokenValidator` en un `@TestConfiguration` (`fake.emitir("raul")`): sin token 401, token válido pasan, expirado 401 con mensaje, refresh token 401, servicio caído 503, GETs públicos |
| `ActorControllerTest`, `PeliculaControllerTest`, `AsignacionActoresTest` | Cambian `TokensPrueba` por el fake (`fake.emitir(...)` en la cabecera). El resto de aserciones intactas |
| `ArquitecturaTest` | Sigue pasando (reglas intactas; el código auth desaparece) |
| Tests del auth-service | Se ejecutan con su propio `mvn test` dentro de `auth-service/` |

## 7) `compose.yaml`

```yaml
services:
  db:            # existente: mariadb, 3307:3306, volumen mariadb_data, healthcheck (sin cambios)
  db-auth:       # NUEVO
    image: mariadb:11
    environment:
      MARIADB_DATABASE: auth
      MARIADB_USER: auth
      MARIADB_PASSWORD: auth
      MARIADB_ROOT_PASSWORD: root
    ports: ["3308:3306"]
    volumes: [auth_data:/var/lib/mysql]
    healthcheck: (igual que db, con mariadb-admin ping)
  auth:          # NUEVO
    build: ./auth-service
    depends_on: { db-auth: { condition: service_healthy } }
    environment:
      SPRING_DATASOURCE_URL: jdbc:mariadb://db-auth:3306/auth
      SPRING_DATASOURCE_USERNAME: auth
      SPRING_DATASOURCE_PASSWORD: auth
    ports: ["8081:8081"]
  app:           # EXISTENTE: se añade dependencia de auth y variable AUTH_SERVICE_URL
    environment:
      AUTH_SERVICE_URL: http://auth:8081

volumes:
  mariadb_data:
  auth_data:     # NUEVO
```

## 8) Punto de atención: reinicio de la BD de desarrollo

La BD `peliculas` local ya tiene aplicadas `V3` y `V4` en su `flyway_schema_history`. Al eliminar
esas migraciones del código de la app, Flyway fallaría en validación ("migraciones aplicadas sin
fichero local"). **Solución: `docker compose down -v`** antes del primer arranque (se pierden los
datos de desarrollo —p. ej. "Matrix"— y se reseedea con `V1`/`V2`). No dejar V3/V4 como migraciones
vacías: ensucia la historia.

## 9) Plan de ejecución (orden estricto)

| Paso | Acción | Verificación |
|---|---|---|
| 1 | **Rama**: `git checkout develop && git checkout -b feature/11-servicio-auth` | Rama creada |
| 2 | **`auth-service/`**: `pom.xml`, mover código (dominio→controllers), migraciones V1/V2 (incluye la semilla `V4` que está sin commitear), `/validate`, `application.properties` | Compila |
| 3 | **Tests auth**: mover los existentes + nuevos tests de `/validate` | `mvn test` en verde dentro de `auth-service/` |
| 4 | **Limpieza app**: borrar código auth, `V3`/`V4`, `spring-security-crypto` | Compila |
| 5 | **Nuevo flujo app**: `TokenValidator`, `AuthNoDisponibleException`, `HttpTokenValidator`, `TokenValidatorConfig`, filtro reescrito | Compila |
| 6 | **Tests app**: fake `TokenValidator` + adaptar los 3 tests de escritura + `HttpTokenValidatorTest` + reescribir filtro | `mvn test` en verde en la raíz; ArchUnit OK |
| 7 | **Docker**: `auth-service/Dockerfile` + `.dockerignore` + `compose.yaml` (servicios `db-auth`, `auth`, env en `app`) | `docker compose config` válido |
| 8 | **Reset BD**: parar instancia local del 8081 si sigue viva; `docker compose down -v && docker compose up -d --build` | 4 contenedores healthy |
| 9 | **Verificación manual** (tabla inferior) | Todo correcto |
| 10 | **Limpieza**: borrar datos de prueba (película creada, tokens del smoke) | BD limpia |
| 11 | **Commit**: rama `feature/11-servicio-auth` → merge `--no-ff` en `develop` → push (previa confirmación) | Historia del repo correcta |

## 10) Verificación manual final

| # | Prueba | Esperado |
|---|---|---|
| 1 | `GET localhost:8080/api/peliculas` sin token | 200 |
| 2 | `POST localhost:8080/api/peliculas` sin token | 401 "Falta la cabecera Authorization" |
| 3 | `POST localhost:8081/api/auth/register` usuario nuevo | 201 (sin tokens; login manual aparte) |
| 4 | `POST localhost:8081/api/auth/register` `admin` (semilla V2) | 409 |
| 5 | `POST localhost:8081/api/auth/login` `admin/admin12345` | 200, `tokenType: Bearer`, `expiresInAccess: 300` |
| 6 | `POST localhost:8081/api/auth/login` password mala | 401 "Credenciales incorrectas" |
| 7 | `POST localhost:8080/api/peliculas` con `Authorization: Bearer <accessToken>` | 201 |
| 8 | `PUT/DELETE /api/actores` con token | pasan (200/204) |
| 9 | `POST localhost:8081/api/auth/refresh` con refresh token | 200 solo accessToken (sin campo `refreshToken`) |
| 10 | Token expirado (5 min) en escritura | 401 "Token expirado" |
| 11 | Refresh token usado como Bearer | 401 |
| 12 | `docker compose stop auth` y repetir la prueba 7 | **503** "Servicio de autenticación no disponible" |
| 13 | `docker compose start auth` | vuelve a funcionar (503 transitorio, sin datos perdidos) |
| 14 | `mvn test` (raíz) y `mvn test` (en `auth-service/`) | ambos en verde |

## 11) Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Acoplamiento HTTP entre servicios | Contrato `/validate` pequeño y cubierto por `HttpTokenValidatorTest` + smoke manual |
| Tests de la app dependen del servicio real | Fakes en memoria (`TokenValidator`) → tests herméticos, sin red |
| Primera build Docker más lenta (2 imágenes) | Aceptarla; capa de `pom.xml` cacheada |
| Los endpoints de auth cambian de puerto (8080 → 8081) | Documentado en README y en este plan |
| Flyway con migraciones "huérfanas" | `docker compose down -v` (sección 8) |
| Rotura de reglas ArchUnit | `HttpTokenValidator` vive fuera de `domain` y `controllers`; se ejecuta `mvn test` completo |
