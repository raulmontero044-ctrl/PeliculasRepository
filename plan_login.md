# Plan: Sistema de login con accessToken + refreshToken

## 1) Resumen de requisitos

- **Proteger solo escrituras**: Únicamente `POST`, `PUT`, `DELETE` de `/api/peliculas` y `/api/actores`. Las operaciones `GET` seguirán siendo públicas.
- **Autenticación por Bearer Token**: `Authorization: Bearer <accessToken>`.
- **Tokens con expiración**: `accessToken` con duración **5 minutos**, `refreshToken` con duración **1 día**.
- **Registro de usuarios**: Necesario un endpoint para crear/registrar usuarios. **Tras registrarse se obliga a iniciar sesión, sin login automático.**
- **Diferenciación de errores en login**: Debemos distinguir entre *"usuario no existe"* y *"credenciales incorrectas"* (con excepciones y mensajes distintos).
- **Solo por expiración**: La única forma de invalidar un token será que haya expirado. **No** vamos a usar el campo `revoked` como mecanismo principal.
- **Refresh**: `POST /api/auth/refresh` devuelve **solo** un nuevo `accessToken` (5 min), el `refreshToken` se mantiene mientras no expire.

## 2) Diseño de dominio

Como el `domain` debe seguir siendo 100% puro (sin Spring, JDBC o anotaciones), añadimos lo siguiente.

### Entidades (POJOs puros)

| Clase | Ubicación | Propósito |
|---|---|---|
| `Usuario.java` | `domain/` | `id`, `username`, `passwordHash`, `enabled`. |
| `AuthToken.java` | `domain/` | `id`, `token`, `userId`, `tipo` (enum), `expiresAt`, `createdAt`. Representará tanto access como refresh. |
| `TokenTipo.java` | `domain/` | Enum puro con `ACCESS` y `REFRESH`. |

### Excepciones de dominio (`domain/exceptions/`)

| Excepción | Cuándo se lanza | HTTP |
|---|---|---|
| `UsuarioYaExisteException.java` | Al intentar registrar un `username` que ya existe. | 409 |
| `UsuarioNoEncontradoException.java` | Cuando se busca un usuario por `username` y **no existe** (específico para login). | 401 |
| `CredencialesIncorrectasException.java` | Cuando el usuario **sí existe** pero la contraseña no coincide. | 401 |
| `TokenInvalidoException.java` | Token malformado, no existe o no corresponde al tipo esperado. | 401 |
| `TokenExpiradoException.java` | Token existe pero `expiresAt < now`. **Única** causa de invalidación. | 401 |

> Nota: `UsuarioNoEncontradoException` y `CredencialesIncorrectasException` **deben distinguirse** para permitir mensajes distintos tal como se pide.

### Puertos (interfaces) (`domain/repository/`)

| Interfaz | Métodos sugeridos |
|---|---|
| `UsuarioRepository.java` | `findByUsername(String username)`, `findById(Long id)`, `existsByUsername(String username)`, `save(Usuario usuario)`. |
| `AuthTokenRepository.java` | `save(AuthToken token)`, `findByToken(String token)`, `findValidByToken(String token, TokenTipo tipo, LocalDateTime now)`. |

### Servicio de dominio (`domain/service/`)

| Clase | Responsabilidades |
|---|---|
| `AuthService.java` | Clase concreta, pura (sin `@Service`). Métodos: `register(String username, String password)` (crea usuario sin generar tokens), `login(String username, String password)` (devuelve par access+refresh), `refresh(String refreshToken)` (genera **solo** un nuevo accessToken), `validateAccessToken(String token)` (usado por el filtro). Toda la lógica de hashes, generación de tokens y comprobación de expiración irá aquí. |

## 3) Persistencia (adaptadores)

| Clase | Ubicación | Propósito |
|---|---|---|
| `UsuarioDao.java` | `persistence/` | `@Repository`, implementa `UsuarioRepository`. Usa `JdbcTemplate` + `UsuarioRowMapper`. |
| `AuthTokenDao.java` | `persistence/` | `@Repository`, implementa `AuthTokenRepository`. Usa `JdbcTemplate` + `AuthTokenRowMapper`. |
| `UsuarioRowMapper.java` | `persistence/rowmapper/` | Mapeo manual de `ResultSet` -> `Usuario`. |
| `AuthTokenRowMapper.java` | `persistence/rowmapper/` | Mapeo manual de `ResultSet` -> `AuthToken`. |

## 4) Controladores, DTOs, seguridad y handlers

### DTOs (`controllers/dto/`)

| DTO | Propósito |
|---|---|
| `RegisterRequest.java` | Campos para registro. Validar con `@NotBlank`. |
| `LoginRequest.java` | `username`, `password` con `@NotBlank`. |
| `TokenPairResponse.java` | Respuesta de `login`: `{ "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer", "expiresInAccess": 300, "expiresInRefresh": 86400 }`. |
| `AccessTokenResponse.java` | Respuesta de `refresh`: `{ "accessToken": "...", "tokenType": "Bearer", "expiresInAccess": 300 }`. |

### Controlador (`controllers/`)

| Clase | Endpoints |
|---|---|
| `AuthController.java` | `POST /api/auth/register` (201, sin tokens), `POST /api/auth/login` (200, par de tokens), `POST /api/auth/refresh` (200, solo accessToken). Solo usa `AuthService`. |

### Seguridad / Filtro

| Clase | Ubicación | Propósito |
|---|---|---|
| `TokenAuthenticationFilter.java` | `controllers/auth/` | `OncePerRequestFilter`. **Solo actúa sobre escrituras**: si el método es `POST`, `PUT` o `DELETE` **Y** la ruta empieza por `/api/peliculas` o `/api/actores` -> exige `Authorization: Bearer <accessToken>`. GETs públicos, rutas `/api/auth/**` sin filtrar. Valida con `AuthService.validateAccessToken` (debe ser tipo `ACCESS`, no expirado). Si válido, permite continuar; si no, lanza excepción para que el handler la gestione. |
| `AuthConfig.java` | `controllers/config/` | Registrar el filtro con `FilterRegistrationBean`. |
| `ServiceConfig.java` | `controllers/config/` | Añadir beans `AuthService` y `BCryptPasswordEncoder` (manteniendo filosofía "domain sin @Service"). |

### Handlers de errores (`controllers/exception/`)

| Handler | Captura | HTTP | Notas |
|---|---|---|---|
| `AuthHandler.java` (nuevo) | `UsuarioNoEncontradoException`, `CredencialesIncorrectasException`, `TokenInvalidoException`, `TokenExpiradoException`, `UsuarioYaExisteException` | `UsuarioYaExisteException` → 409. Las demás de auth → 401 con **mensajes diferenciados** ("Usuario no existe" vs "Credenciales incorrectas"). Formato idéntico a `ErrorRespuesta`. |

## 5) Base de datos (Flyway)

Manteniendo esquema y datos separados.

### `src/main/resources/db/migration/V3__create_auth_tables.sql`

```sql
CREATE TABLE usuarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT uk_usuarios_username UNIQUE (username)
);

CREATE TABLE auth_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(128) NOT NULL,
  user_id BIGINT NOT NULL,
  tipo VARCHAR(10) NOT NULL,        -- 'ACCESS' o 'REFRESH'
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_auth_tokens_token UNIQUE (token),
  CONSTRAINT fk_auth_tokens_usuario FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
  CONSTRAINT chk_auth_tokens_tipo CHECK (tipo IN ('ACCESS','REFRESH'))
);

CREATE INDEX idx_auth_tokens_token ON auth_tokens(token);
CREATE INDEX idx_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_expires_at ON auth_tokens(expires_at);
```

**Decisiones clave:**
- **Sin `revoked`**: Solo se valida por `expires_at`. Invalidación únicamente por expiración.
- **`tipo` separado**: Distingue `ACCESS` (5 min) vs `REFRESH` (1 día). El filtro solo acepta `ACCESS`.
- **Índices**: En `token`, `user_id` y `expires_at`.

## 6) Dependencias Maven

Añadir únicamente:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

Usar `BCryptPasswordEncoder` para hash y verificación. **No** añadir `spring-boot-starter-security` completo.

## 7) Flujo detallado

### Registro
1. `POST /api/auth/register` con `RegisterRequest {username, password}`
2. `AuthService.register`: comprueba unicidad -> si existe lanza `UsuarioYaExisteException` (409). Genera hash BCrypt, crea usuario `enabled=true`, persiste. **No genera tokens**.
3. Respuesta 201.

### Login
1. `POST /api/auth/login` con `LoginRequest {username, password}`
2. `AuthService.login`:
   - Busca por username. **Si no existe** → `UsuarioNoEncontradoException` (401, "Usuario no existe")
   - Si existe pero `password` no coincide → `CredencialesIncorrectasException` (401, "Credenciales incorrectas")
   - Si válido → genera `accessToken` (5 min) y `refreshToken` (1 día), persiste ambos con su tipo/expiresAt, devuelve `TokenPairResponse`
3. Mensajes diferenciados obligatorios.

### Refresh
1. `POST /api/auth/refresh` con `{"refreshToken": "..."}`
2. `AuthService.refresh`:
   - Busca token por valor, debe ser tipo `REFRESH`
   - **Solo por expiración**: si `expiresAt < now` → `TokenExpiradoException` (401)
   - Si válido → genera **únicamente** nuevo `accessToken` (5 min), lo persiste, devuelve `AccessTokenResponse`. No rota refreshToken.
3. Si expirado → 401 por expiración.

### Protección de escrituras
1. `TokenAuthenticationFilter` intercepta solo si `method` ∈ {POST,PUT,DELETE} y URI empieza por `/api/peliculas` o `/api/actores`
2. Extrae `Authorization: Bearer <token>`. Si falta/corrupto → 401
3. `AuthService.validateAccessToken`: debe ser tipo `ACCESS`, existir, **y `expiresAt >= now`** (solo expiración). Si expirado → `TokenExpiradoException` (401). Si válido → permite continuar.

## 8) Integración con ArchUnit y reglas existentes

- **Domain puro**: `AuthService`, entidades, excepciones, puertos sin imports Spring/JDBC.
- **Controladores no importan persistence**: `AuthController` y filtro solo usan `AuthService`. DTOs solo en `controllers/dto`.
- **Sin Lombok**: Todo con getters/setters a mano.
- **Servicios registrados como beans**: `AuthService` y `BCryptPasswordEncoder` en `ServiceConfig`.
- **Excepciones en `domain/exceptions`**, handlers en `controllers/exception`.
- **ArchUnit**: Debe seguir pasando tras los cambios (verificar las 4 reglas).

## 9) Plan de ejecución TDD (orden estricto)

| Paso | Acción | Verificación |
|---|---|---|
| 1 | **Rama**: `git checkout develop && git pull && git checkout -b feature/10-autenticacion-token` | Rama creada desde develop |
| 2 | **Dependencias**: Añadir `spring-security-crypto` al `pom.xml` | Compila |
| 3 | **Migraciones**: Crear `V3__create_auth_tables.sql` | Flyway válido, compatible con H2 en tests |
| 4 | **Dominio**: Crear `Usuario`, `AuthToken`, `TokenTipo`, excepciones (`UsuarioYaExiste`, `UsuarioNoEncontrado`, `CredencialesIncorrectas`, `TokenInvalido`, `TokenExpirado`), puertos `UsuarioRepository`/`AuthTokenRepository`. **Tests unitarios primero**. | Rojo->Verde, sin Spring |
| 5 | **Persistencia**: `UsuarioRowMapper`, `AuthTokenRowMapper`, `UsuarioDao`, `AuthTokenDao`. Tests con contexto JDBC/test. | Guardado/búsqueda correcto |
| 6 | **Servicio**: `AuthService` con `BCryptPasswordEncoder`. Tests TDD: registro sin tokens (201 lógica), duplicado → 409, login usuario no existe vs credenciales incorrectas (401 diferenciados), login ok genera par, refresh solo access, expiración solo por expiresAt. | Todos verdes |
| 7 | **DTOs + Controller**: `RegisterRequest`, `LoginRequest`, `TokenPairResponse`, `AccessTokenResponse`, `AuthController`. Tests `@WebMvcTest`. | `register` 201 sin tokens, `login`/`refresh` correctos |
| 8 | **Seguridad/Filtro**: `TokenAuthenticationFilter`, `AuthConfig` con `FilterRegistrationBean`. Tests: escrituras sin token → 401, con accessToken válido → pasan, GETs públicos siguen accesibles, accessToken expirado → 401, refreshToken no sirve para escrituras. | Protección solo a escrituras correcta |
| 9 | **Handlers**: `AuthHandler` con mapeo 401/409 y **mensajes diferenciados**. | Errores consistentes con formato `ErrorRespuesta` |
| 10 | **Integración + ArchUnit**: Ejecutar `mvn test` completo. Verificar tests existentes verdes y reglas ArchUnit OK. | Sin regresiones |
| 11 | **Merge**: `git checkout develop && git merge --no-ff feature/10-autenticacion-token && git push`. **No borrar la rama**. | Cumple flujo del plan |