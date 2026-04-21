# Guía de estilo y arquitectura - GOA (v2)

Documento normativo para contribuir en GOA. Define arquitectura, convenciones y límites de cada capa.

## Niveles de regla

- `DEBE`: obligatorio.
- `DEBERÍA`: recomendado, salvo razón técnica explícita.
- `PUEDE`: opcional.

## Arquitectura general

Sistema de microservicios con Eureka + API Gateway + servicios Spring Boot.

```
eureka
  ->
gateway
  ->
microservicios (goa-user, goa-support, ...)
  <-
commons (Java puro)
```

## Commons
`commons` contiene componentes transversales reutilizables.
- DEBE no depender de Spring.
- DEBE usar solo JDK y librerías neutrales.
- DEBE incluir solo piezas usadas por varios microservicios.
- DEBERÍA mantener contratos estables y pequeños.
- PUEDE crearse `commons-spring` en el futuro si aparece una necesidad real.

Regla de inclusión:
- Si lo usan varios microservicios y es Java puro -> `commons`.
- Si lo usa un solo microservicio -> se queda en ese microservicio.
- Si requiere Spring -> no va a `commons`.

## Estructura de un microservicio
```
es.upm.api/
  configurations/
  data/
    daos/
    entities/
  resources/
    dtos/
      validations/
  services/
    criteria/
  integrations/
    dtos/
  infrastructure/
```

## Recursos (HTTP)
- DEBE usar `@RestController` y sufijo `Resource`.
- DEBE convertir DTO <-> entidad en `resources`, nunca en `services`.
- DEBE delegar lógica de negocio al servicio.
- DEBE definir rutas base como constantes (`public static final String ...`).
- DEBE usar inyección por constructor (`@RequiredArgsConstructor`).
- DEBE aplicar seguridad con `@PreAuthorize`.
- DEBERÍA definir una regla base en clase y reglas específicas en métodos.
- DEBERÍA validar entrada con `@Valid` y regex en `Validations`.

## DTOs
- DEBE usar sufijo `Dto`.
- DEBE vivir en `resources.dtos` (o `integrations.dtos` para contratos Feign).
- DEBERÍA usar un único `XxxDto` cuando E/S es similar.
- DEBERÍA separar en `XxxCreationDto` y `XxxUpdateDto` cuando E/S diverge claramente.
- DEBERÍA usar `@JsonProperty(access = READ_ONLY/WRITE_ONLY)` para asimetrías pequeñas.
- DEBERÍA separar DTOs si más de la mitad de los campos son asimétricos.
- PUEDE incluir constructor `XxxDto(Entity)` y métodos `toEntity()` / `ofXxx()`.

## Criterios

- DEBE vivir en `services.criteria`.
- DEBE usar sufijo `FindCriteria`.
- DEBE representar parámetros estructurados de búsqueda.
- NO DEBE incluir anotaciones Jackson o validaciones HTTP.
- DEBERÍA construirse en resource con `@ModelAttribute`.
- PUEDE incluir `all()` para detectar ausencia de filtros.

## Servicios

- DEBE usar `@Service` y sufijo `Service`.
- DEBE contener la lógica de negocio.
- DEBE trabajar con entidades, no con DTOs.
- DEBERÍA usar nombres consistentes:
  - `create(...)`
  - `read(...)` / `readByX(...)`
  - `update(...)` / `updateByX(...)`
  - `delete(...)` / `deleteByX(...)`
  - `find(criteria)`
- DEBE lanzar `NotFoundException` en `read/update/delete` cuando no exista recurso.
- DEBERÍA usar privados `assertXxx(...)` para invariantes.

## Persistencia (Data)

- DEBE usar convenciones Spring Data: `findByX`, `existsByX`, `countByX`.
- DEBE ubicar `Repository`, `RepositoryCustom` y `RepositoryCustomImpl` en el mismo paquete.
- DEBERÍA usar `MongoTemplate` + `Criteria` para queries complejas.
- PUEDE usar `@Query` para consultas puntuales.

## Entidades

- DEBE ser clases `@Document` sin sufijo.
- DEBERÍA contener solo datos y lógica de dominio mínima.
- DEBE marcar id con `@Id`.
- DEBERÍA usar `@Indexed(unique = true)` en campos únicos.
- PUEDE usar `@DBRef` cuando la relación lo justifique.

## Integraciones (Feign)

- DEBE usar `@FeignClient` para llamadas entre microservicios.
- DEBE duplicar DTOs de contrato por microservicio para evitar acoplamiento en `commons`.

## Infraestructura

- DEBE contener utilidades técnicas específicas del microservicio.
- NO DEBE contener lógica de negocio.
- DEBERÍA separarse en subpaquetes cuando crezca (por ejemplo `infrastructure/email`).

## Seguridad

- DEBE proteger endpoints con `@PreAuthorize`.
- DEBE usar constantes SpEL de `es.upm.miw.security.Security`.
- NO DEBE escribir expresiones SpEL literales en anotaciones.
- DEBERÍA mantener la regla visible en el propio método para facilitar revisión de PR.

## Excepciones y errores

Excepciones de dominio en `commons` (todas `RuntimeException`):

- `BadRequestException` -> 400
- `ForbiddenException` -> 403
- `NotFoundException` -> 404
- `ConflictException` -> 409
- `InternalServerException` -> 500
- `BadGatewayException` -> 502

Convenciones:

- DEBERÍA incluir constructor con `String detail`.
- DEBERÍA incluir constructor con `Throwable cause` al envolver errores.
- DEBERÍA usar `IllegalStateException` para estados imposibles.
- DEBE mantener excepciones específicas de un servicio dentro de ese servicio.

## Inicializadores

- DEBE implementarse como `ApplicationRunner` con `@Order`.
- DEBE anotarse con `@Component`.
- DEBE ejecutar lógica en `run(...)`, no en constructor.
- `SeederForDev` DEBE activarse solo con perfiles `dev` y `test`.
- DEBERÍA usar ids fijos en seeder cuando los tests los referencien.

## Tests

Niveles esperados:

- Unitarios: `XxxTest` sin contexto Spring.
- Integración: `XxxIT` (o convención local), con `@SpringBootTest` y perfil `test`.
- Funcionales: `XxxFT` con HTTP real (`RANDOM_PORT` + `TestRestTemplate`).

Reglas:

- DEBE cubrir casos felices y de error de seguridad.
- DEBE restaurar estado cuando un test muta datos compartidos.
- DEBERÍA usar `@WithMockUser` en integración cuando aplique.

## Resumen de nombres

| Tipo | Sufijo | Paquete |
|---|---|---|
| Entidad persistencia | sin sufijo | `data.entities` |
| Repositorio | `Repository` | `data.daos` |
| Repo custom | `RepositoryCustom` + `RepositoryCustomImpl` | `data.daos` |
| Servicio | `Service` | `services` |
| Criterio | `FindCriteria` | `services.criteria` |
| Controlador | `Resource` | `resources` |
| DTO HTTP | `Dto` / `CreationDto` / `UpdateDto` | `resources.dtos` |
| Cliente Feign | `WebClient` | `integrations` |
| DTO Feign | `Dto` | `integrations.dtos` |
| Excepción dominio | `Exception` | `es.upm.miw.exception` |

## Tecnología y formato

- Java 21.
- Spring Boot 3.5+.
- Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Log4j2`).
- JUnit 5 + AssertJ.
- MapStruct no se usa.
- Conversión DTO <-> entidad con `BeanUtils.copyProperties` y builders.
- Comentarios en español; código y logs en inglés (salvo excepciones justificadas).

## Antipatrones (prohibidos)

- DTOs en capa `services`.
- Lógica en constructores de beans.
- Exponer entidades directamente en resources.
- SpEL literal en `@PreAuthorize`.
- Dependencias Spring dentro de `commons`.
