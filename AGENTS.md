# Guia de estilo y arquitectura - GOA User (v4)

Documento normativo para contribuir en `goa-user`.
Esta version refleja el estado real del codigo (auditoria: 2026-04-30).

## Niveles de regla

- `DEBE`: obligatorio.
- `DEBERIA`: recomendado, salvo razon tecnica explicita.
- `PUEDE`: opcional.

## Arquitectura general

Sistema de microservicios con Eureka + API Gateway + servicios Spring Boot.

```text
eureka
  ->
gateway
  ->
microservicios (goa-user, goa-support, ...)
  <-
commons (goa-commons)
```

## Estructura real del microservicio

```text
es.upm.api/
  configurations/
  data/
    daos/
    entities/
  exceptions/
  infrastructure/
    support/
    clients/
      email/
  resources/
    dtos/
    httperrors/
  services/
    criteria/
```

Notas:
- No existe paquete `integrations/`; los clientes externos se ubican en `infrastructure.clients`.
- `infrastructure.support` contiene utilidades tecnicas reutilizables por servicios.

## Recursos (HTTP)

- DEBE usar `@RestController` y sufijo `Resource`.
- DEBE delegar logica de negocio al servicio.
- DEBE usar rutas base como constantes (`public static final String ...`).
- DEBE usar inyeccion por constructor (`@RequiredArgsConstructor`).
- DEBE proteger metodos con `@PreAuthorize` usando constantes de `es.upm.miw.security.Security`.
- NO DEBE usar SpEL literal en `@PreAuthorize`.
- DEBERIA validar entrada con `@Valid` y regex de `Validations`.

## DTOs

- DEBE ubicarse en `resources.dtos`.
- DEBE seguir esta convencion:
  - `XxxResponseDto`: solo salida.
  - `XxxCreationDto`: solo entrada de creacion.
  - `XxxUpdatingDto`: solo entrada de actualizacion.
  - `XxxDto`: entrada/salida, marcando asimetrias con
    `@JsonProperty(access = Access.READ_ONLY)` y
    `@JsonProperty(access = Access.WRITE_ONLY)`.
- DEBE mantener conversion DTO <-> entidad en capa `resources.dtos` (constructores y `toDomain()`).
- NO DEBE mover DTOs a capa `services`.

DTOs actuales:
- `UserDto`
- `UserAndConsentUpdatingDto`
- `DataProcessingConsentCreationDto`
- `DataProcessingConsentResponseDto`
- `AccessLinkCreationDto`
- `AccessLinkResponseDto`
- `ProvincesResponseDto`

## Criterios

- DEBE vivir en `services.criteria`.
- DEBE usar sufijo `FindCriteria`.
- DEBERIA recibirse en resources via `@ModelAttribute`.
- NO DEBE contener anotaciones de serializacion HTTP.

## Servicios

- DEBE usar `@Service` y sufijo `Service`.
- DEBE trabajar con entidades (no con DTOs).
- DEBERIA mantener nombres consistentes: `create`, `read`, `update`, `delete`, `find`.
- DEBE lanzar `NotFoundException` en `read/update/delete` cuando no exista recurso.
- DEBERIA encapsular invariantes en metodos privados (`assertXxx`, `validateXxx`, etc.).

## Persistencia (Mongo)

- DEBE usar `MongoRepository` + `RepositoryCustom` + `RepositoryCustomImpl` en el mismo paquete.
- DEBERIA usar `MongoTemplate` + `Criteria` para consultas complejas.
- DEBE usar convenciones Spring Data en metodos simples (`findByX`, `existsByX`, etc.).

## Entidades

- DEBE ser `@Document` sin sufijo.
- DEBE marcar id con `@Id`.
- DEBERIA usar `@Indexed(unique = true)` en campos unicos.
- PUEDE usar `@DBRef` si la relacion lo requiere.

Entidades de coleccion actuales:
- `User`
- `AccessLink`
- `DataProcessingConsent`

Enums de dominio:
- `Role`
- `Province`

## Infrastructure support

- DEBE contener utilidades tecnicas internas reutilizables.
- NO DEBE contener logica de negocio.
- Servicios actuales:
  - `EncryptionService`
  - `LegalPolicyService`
  - `ProfileUpdatedEmailTemplateService`

## Infrastructure clients

- DEBE contener clientes/adaptadores de salida a otros microservicios.
- DEBE aislar detalles de protocolo/integracion (Feign, headers, etc.).
- Cliente actual:
  - `infrastructure.clients.email.GoaSupportClient`

## Seguridad

- DEBE proteger endpoints con `@PreAuthorize`.
- DEBE usar constantes de `Security`.
- DEBE mantener visible la regla de autorizacion en cada metodo sensible.
- DEBERIA mantener coherencia entre:
  - reglas de `@PreAuthorize` en resources
  - reglas de permitidos en `ResourceServerConfig`.

## Excepciones y errores

- DEBE usar excepciones de dominio de `es.upm.miw.exception` y excepciones locales de `es.upm.api.exceptions` cuando aplique.
- DEBE centralizar mapeo HTTP en `resources.httperrors.ApiExceptionHandler`.
- NO DEBE declarar excepciones en paquetes de entidades.

## Inicializadores y seeders

- DEBE implementarse con `ApplicationRunner`.
- DEBE ejecutar logica en `run(...)`.
- `SeederForDev` DEBE limitarse a perfiles `dev` y `test`.
- DEBERIA usar ids fijos cuando los tests dependan de esos ids.

## Tests

Convencion actual:
- Unitarios: `*Test`.
- Integracion: `*IT`.
- Funcionales HTTP: `*FT` (`RANDOM_PORT` + `TestRestTemplate`).

Reglas:
- DEBE cubrir casos felices y de error (autorizacion incluida).
- DEBE restaurar estado cuando el test muta datos compartidos.
- DEBERIA usar `@WithMockUser` en pruebas de servicio que dependan de rol.

## Tecnologia y build

- Java objetivo del proyecto: **21**.
- Spring Boot: `3.5.x`.
- Spring Cloud: `2025.0.x`.
- Lombok: `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Log4j2`.
- Conversion DTO <-> entidad con `BeanUtils.copyProperties` y builders.

Regla de entorno:
- DEBE compilarse con JDK 21 para evitar problemas de annotation processing.
- Si se usa JDK 23+, DEBERIA activarse annotation processing de forma explicita (ejemplo CLI: `-Dmaven.compiler.proc=full`) o configurar processors en `maven-compiler-plugin`.

## Antipatrones prohibidos

- DTOs en capa `services`.
- Exponer entidades directamente desde resources.
- SpEL literal en `@PreAuthorize`.
- Logica de negocio en constructores de beans.
- Mezclar utilidades internas (`support`) con clientes externos (`clients`) en el mismo paquete.
