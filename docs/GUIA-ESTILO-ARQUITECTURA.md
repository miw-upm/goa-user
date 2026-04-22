# Guía de estilo y arquitectura — GOA

Documento de referencia para entender cómo está organizado el código y qué convenciones seguir al añadir nuevas piezas.

## Arquitectura general

Sistema de microservicios con **Eureka** (descubrimiento), **API Gateway** (Spring Cloud Gateway) y microservicios
Spring Boot. Una librería **commons** compartida sin dependencias de Spring agrupa lo transversal.

```
eureka (descubrimiento)
  ↓
gateway (entrada única)
  ↓
microservicios (goa-user, goa-support, …)
  ↑
commons (librería Java pura, sin Spring)
```

## Commons

Contiene lo que **cualquier microservicio** puede necesitar. Regla estricta: **cero dependencias de Spring**. Solo JDK y
librerías neutras.

Paquetes:

- `es.upm.miw.exception` — excepciones de dominio (`NotFoundException`, `BadRequestException`, `ConflictException`,
  `ForbiddenException`, `InternalServerException`, `BadGatewayException`) y `ErrorMessage` para la respuesta al cliente.
  Todas extienden `RuntimeException`.
- `es.upm.miw.security.Security` — constantes SpEL para `@PreAuthorize` (`ADMIN`, `ADMIN_MANAGER_OPERATOR`, etc.). Son
  strings literales, sin dependencia del enum `Role`.
- `es.upm.miw.device` — `DeviceInfo`, `DeviceInfoResolver` para extraer información del navegador/dispositivo desde
  headers HTTP.
- `es.upm.miw.uuid` — `UUIDBase64` para generar IDs cortos base64 URL-safe.
- `es.upm.miw.badge` — `VersionBadgeGenerator`, genera SVG tipo shields.io. Clase utility con constructor privado y
  métodos `static`.
- `es.upm.miw.mail` — `EmailTemplateRenderer` para renderizar templates HTML con placeholders.

Regla de entrada a commons:

> Si varios microservicios lo usan y es puro JDK, a commons. Si solo lo usa uno, se queda en su microservicio. Si
> necesita Spring, se queda en el microservicio (o crearíamos un `commons-spring` separado, cuando haga falta).

## Capas del microservicio

Arquitectura de tres capas:

```
es.upm.api/
  configurations/            ← beans de configuración, inicializadores
  data/
    daos/                    ← repositorios Spring Data + customs
    entities/                ← entidades de persistencia (MongoDB)
  resources/                 ← controllers HTTP (capa de presentación)
    dtos/                    ← DTOs de entrada/salida
      validations/           ← patrones de validación (regex de path y body)
  services/                  ← lógica de negocio
    criteria/                ← criterios estructurados para búsquedas
  integrations/              ← Feign clients hacia otros microservicios
    dtos/                    ← DTOs del contrato con otros servicios
  infrastructure/            ← utilidades técnicas (lectura de ficheros legales, templates)
```

### Resources (HTTP)

Clases con `@RestController`, sufijo **`Resource`**. Una por entidad expuesta. Responsabilidades: deserializar entrada,
validar (`@Valid`), llamar al servicio, proyectar salida con DTOs.

Convenciones:

- **Rutas como constantes** del propio resource: `public static final String USERS = "/users";`.
- **`@PreAuthorize` a nivel clase** para la regla base (`Security.AUTHENTICATED`), más específico a nivel método (
  `Security.ADMIN_MANAGER_OPERATOR`). Se coloca *in situ* para que cada endpoint diga quién puede acceder.
- **Patrones de path con regex** vía `Validations.ID_WITH_UUID`, `Validations.ID_WITH_MOBILE` — desambiguan rutas y
  rechazan IDs mal formados antes de llegar al controller.
- **Inyección por constructor** con `@RequiredArgsConstructor` de Lombok.
- **Nada de lógica de negocio**: los resources solo traducen HTTP → parámetros del servicio.
- **Conversión DTO ↔ entidad** aquí, nunca en el servicio.

Ejemplo típico:

```java
@PreAuthorize(Security.AUTHENTICATED)
@RestController
@RequiredArgsConstructor
@RequestMapping(UserResource.USERS)
public class UserResource {
    public static final String USERS = "/users";
    private final UserService userService;

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
    @GetMapping(Validations.ID_WITH_UUID)
    public UserDto readById(@PathVariable UUID id) {
        return new UserDto(this.userService.read(id));
    }
}
```

### DTOs

Objetos de transporte entre capas. Sufijo **`Dto`** siempre. Las entidades nunca llevan sufijo.

Filosofía pragmática:

- **Un solo `XxxDto`** cuando entrada y salida comparten la mayoría de campos (ej. `UserDto`).
- **`XxxCreationDto` / `XxxUpdateDto`** separados cuando la entrada diverge claramente de la salida (ej.
  `AccessLinkCreationDto` solo tiene 2 campos mientras `AccessLinkDto` tiene 8).
- **Wrappers de entrada compuesta**: `XxxWithYyyDto` cuando un endpoint recibe dos conceptos (ej.
  `UserUpdateWithConsentDto`).

#### Control de dirección en un DTO compartido

Cuando un mismo `XxxDto` se usa en E/S, se marcan campos asimétricos con Jackson:

- `@JsonProperty(access = Access.READ_ONLY)` — sale en respuestas, se ignora en peticiones (IDs generados, timestamps
  del servidor).
- `@JsonProperty(access = Access.WRITE_ONLY)` — entra en peticiones, nunca sale en respuestas (passwords, tokens).

Regla: si más de la mitad de los campos son asimétricos, mejor separar en dos DTOs. Si son 2-3, anotaciones.

#### Métodos de un DTO

- **Constructor `XxxDto(Entity entity)`** para convertir entidad → DTO, usando `BeanUtils.copyProperties`.
- **`toEntity()`** para convertir DTO → entidad (típicamente `toUser()`, `toCreation()`, etc.).
- **Proyecciones `ofXxx()`** para devolver un DTO reducido: `ofBasic()`, `ofMobileFirstNameFamilyNameEmail()`. El nombre
  describe qué campos lleva. Se construyen encadenando builders.

### Criteria

Clases en `services/criteria/` para agrupar parámetros de búsqueda. Sufijo **`FindCriteria`**.

- **Anotación `@ModelAttribute`** en el resource: Spring los construye desde query params.
- **No son DTOs HTTP**: no llevan validaciones Jakarta ni anotaciones Jackson. Son inputs estructurados al servicio.
- Método opcional **`all()`** para saber si no se ha especificado ningún filtro.

### Services

Clases con `@Service`, sufijo **`Service`**. Contienen la lógica de negocio: orquestan repositorios, validaciones,
enriquecimiento de entidades, llamadas a otros servicios.

Convenciones de nombres de métodos:

| Método                           | Devuelve            | Si no encuentra                |
|----------------------------------|---------------------|--------------------------------|
| `create(entity)`                 | void o la entidad   | —                              |
| `read(id)` / `readByX(x)`        | entidad             | `NotFoundException`            |
| `update(...)` / `updateByX(...)` | entidad actualizada | `NotFoundException`            |
| `delete(...)` / `deleteByX(...)` | void                | `NotFoundException` o silencio |
| `find(criteria)`                 | `Stream<Entity>`    | stream vacío                   |

Métodos privados auxiliares:

- **`assertXxx(...)`**: lanza excepción si se viola una invariante (`assertNoExistByMobile`, `assertNoExistByEmail`). El
  prefijo `assert` deja claro que no devuelven nada y pueden lanzar.

Los servicios **trabajan con entidades**, nunca con DTOs. La conversión DTO↔entidad ocurre en el resource.

### Persistencia (Data)

Patrón Spring Data con extensiones custom cuando hacen falta queries complejas:

```
data/daos/
  UserRepository.java                 (interface, extiende MongoRepository + UserRepositoryCustom)
  UserRepositoryCustom.java           (interface con los métodos custom)
  UserRepositoryCustomImpl.java       (implementación con MongoTemplate)
```

**Importante**: las tres clases viven en **el mismo paquete** porque Spring Data busca la `*Impl` ahí.

Convenciones:

- Nombres de métodos `findByX`, `existsByX`, `countByX` siguen convención Spring Data.
- Queries complejas con regex y filtros opcionales → `CustomImpl` usando `MongoTemplate` y `Criteria`.
- Anotación `@Query` para queries Mongo a mano cuando hace falta.

### Entidades

Clases con `@Document`, sin sufijo. Solo datos + métodos de negocio mínimos (`User.fullName()`, `AccessLink.use()`).

- `@Id` en el identificador.
- `@Indexed(unique = true)` para campos únicos.
- `@DBRef` para referencias entre documentos.
- Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.

### Integrations (Feign)

Clientes hacia otros microservicios. Interfaz con `@FeignClient`.

```
integrations/
  SupportWebClient.java          (interfaz Feign hacia goa-support)
  dtos/
    EmailDto.java                (DTO del contrato)
```

Los DTOs del contrato se **duplican en cada microservicio** a propósito — evita acoplar commons con contratos
específicos entre dos servicios concretos.

### Infrastructure

Utilidades técnicas específicas del microservicio que no son lógica de negocio. Ejemplos:

- `LegalPolicyService` — lee ficheros `lopd.YYYY-MM-DD.html` del classpath.
- `ProfileUpdatedEmailTemplateService` — renderiza emails desde templates HTML.

Cuando un subgrupo crezca (ej. varios templates de email), extraer a subpaquete (`infrastructure/email/`).

## Seguridad

Flujo OAuth2 con JWT emitido por el propio `goa-user` (authorization server). Los demás microservicios son resource
servers que validan el JWT.

### Roles

Enum `Role` en `data.entities` (solo lo usa `goa-user` directamente):

- `ADMIN`, `MANAGER`, `OPERATOR`, `CUSTOMER`, `URL_TOKEN`, `ANONYMOUS`, `AUTHENTICATED`.
- `Role.from(String)` normaliza valores con prefijo `ROLE_` y mayúsculas/minúsculas.
- `Role.springSecurityAuthority()` devuelve `ROLE_admin`, etc.
- `Role.jwtClaimValue()` devuelve el string que viaja en el JWT (`admin`, `manager`, …).

### `@PreAuthorize`

Se usa **siempre** a nivel método (o clase si aplica a todos). Las expresiones son constantes SpEL de `Security`:

```java
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@GetMapping
public List<UserDto> find(...) { ... }
```

Ventajas del enfoque:

- Cada endpoint se auto-documenta con su regla de acceso.
- Los diffs de PR muestran cambios de seguridad junto al endpoint afectado.
- Las constantes evitan typos y permiten refactor seguro.

## Excepciones y manejo de errores

Jerarquía en commons, todas `RuntimeException`:

| Excepción                 | HTTP | Cuándo                                      |
|---------------------------|------|---------------------------------------------|
| `BadRequestException`     | 400  | Entrada mal formada, parámetros inválidos   |
| `ForbiddenException`      | 403  | Autenticado pero sin permiso                |
| `NotFoundException`       | 404  | Recurso no existe                           |
| `ConflictException`       | 409  | Conflicto de estado (ej. unique constraint) |
| `InternalServerException` | 500  | Fallo inesperado del servicio               |
| `BadGatewayException`     | 502  | Fallo al comunicar con otro servicio        |

Convenciones:

- **Constructor con `String detail`**: el mensaje incluye el nombre de la excepción para logs.
- **Constructor con `Throwable cause`**: cuando envolvemos otra excepción, conservamos el stack trace original.
- **`IllegalStateException` para errores que "no deberían pasar"**: recursos del classpath que no cargan, invariantes
  rotas. Se propagan como 500 automáticamente.
- **Excepciones específicas del microservicio** (ej. `BadCredentialsException` en `goa-user`) se quedan en el
  microservicio.

## Inicializadores

Patrón `ApplicationRunner` con `@Order`:

- **`AdminUserInitializer`** — siempre activo (`@Order(2)`). Crea un admin si no existe. Usa `log.warn` al crearlo
  porque la ausencia de admin en un sistema maduro es anómala.
- **`SeederForDev`** — solo activo con `@Profile({"dev", "test"})` y `@Order(1)`. Borra todo y siembra datos de prueba.

Convenciones:

- **`@Component`** (no `@Repository` ni `@Service`).
- **Lógica en `run(ApplicationArguments)`**, no en el constructor — así el contexto está listo antes de tocar BD.
- **IDs hardcodeados** en el seeder (`aaaa-bbbb-cccc-dddd-eeeeffff####`) para que los tests puedan referenciarlos.

## Tests

Tres niveles:

### Unitarios (sin Spring)

`XxxTest`. Instancian la clase con `new` y validan con AssertJ. Rápidos, sin contexto.

### Integración (`SpringBootTest` + BD real)

`XxxIT` o `XxxTest` según convención del módulo. Cargan contexto, usan `@ActiveProfiles("test")`, pueden mockear
colaboradores externos con `@MockitoBean`.

- `@WithMockUser(username="...", roles={"manager"})` para simular autenticación.
- Los tests que modifican datos **restauran el estado al final** para no contaminar a los siguientes.

### Funcionales (end-to-end HTTP)

`XxxFT` con `@SpringBootTest(webEnvironment = RANDOM_PORT)` y `TestRestTemplate`. Prueban el servicio completo vía HTTP
real.

Helper `HttpRequestBuilder` en `functionaltests/` encapsula la obtención del token OAuth2 y el builder de peticiones:

```java
this.httpRequestBuilder.post(URL)
    .body(dto)
    .role(ADMIN)
    .exchange(ResponseDto.class);
```

## Naming — resumen

| Tipo                  | Sufijo                                      | Paquete                          |
|-----------------------|---------------------------------------------|----------------------------------|
| Entidad persistencia  | — (sin sufijo)                              | `data.entities`                  |
| Repositorio           | `Repository`                                | `data.daos`                      |
| Repositorio custom    | `RepositoryCustom` + `RepositoryCustomImpl` | `data.daos` (mismo que el repo)  |
| Servicio              | `Service`                                   | `services`                       |
| Criteria              | `FindCriteria`                              | `services.criteria`              |
| Resource (controller) | `Resource`                                  | `resources`                      |
| DTO E/S simétrica     | `Dto`                                       | `resources.dtos`                 |
| DTO solo entrada      | `CreationDto` / `UpdateDto`                 | `resources.dtos`                 |
| Feign client          | `WebClient`                                 | `integrations`                   |
| DTO Feign             | `Dto`                                       | `integrations.dtos`              |
| Excepción de dominio  | `Exception`                                 | `es.upm.miw.exception` (commons) |

## Formato

- **Java 21**, Spring Boot 3.5+.
- **Lombok** para boilerplate: `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Log4j2`.
- **MapStruct no se usa** — conversiones DTO↔entidad con `BeanUtils.copyProperties` y builders.
- **AssertJ** para aserciones.
- **JUnit 5** para tests.
- **Lenguaje de los mensajes**: comentarios en español, código y logs en inglés (con excepciones puntuales).

## Patrones que evitamos

- **DTOs en `services`**: los DTOs viven solo en `resources`. Si un servicio agrupa parámetros, usa un tipo propio (
  record o clase sin anotaciones HTTP) en su propio paquete.
- **Lógica en constructores**: rompe tests y orden de inicialización. Usar `ApplicationRunner`, `@PostConstruct` o
  métodos del bean.
- **Entidades directamente en resources**: siempre intermediamos con DTOs.
- **Strings SpEL literales en `@PreAuthorize`**: siempre constantes de `Security`.
- **Commons con Spring**: rompe la filosofía de biblioteca neutra.
