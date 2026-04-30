# Endpoints API (`goa-user`)

Listado actualizado de endpoints expuestos por los resources actuales, incluyendo DTOs HTTP y entidades de base de datos implicadas.

## SystemResource (`/system`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| GET | `/system` | `applicationInfo` | - | `ApplicationInfoDto` | - | Publica |
| GET | `/system/version-badge` | `generateBadge` | - | `byte[]` (`image/svg+xml`) | - | Publica |

## UserResource (`/users`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| POST | `/users` | `create` | `UserDto` | - | `User` | `permitAll` |
| GET | `/users/{id}` (`UUID`) | `readById` | - | `UserDto` | `User` | `ADMIN_MANAGER_OPERATOR_URL_TOKEN` |
| GET | `/users/{id}` (`mobile`) | `readByMobile` | - | `UserDto` | `User` | `ADMIN_MANAGER_OPERATOR_URL_TOKEN` |
| PUT | `/users/{id}` (`UUID`) | `update` | `UserDto` | `UserDto` | `User` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/users` | `find` | `UserFindCriteria` (`@ModelAttribute`) | `List<UserDto>` | `User` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/users/full` | `findAllFull` | - | `List<UserDto>` (completo, JSON) | `User` | `ADMIN` |
| GET | `/users/provinces` | `findProvinces` | - | `ProvincesResponseDto` | `Province` (enum) | `permitAll` |
| GET | `/users/{scope}/{id}/{token}` | `readByUrlIdWithToken` | - | `UserDto` (vista profile) | `User`, `AccessLink` | `permitAll` |
| PUT | `/users/{scope}/{id}/{token}` | `updateByUrlIdWithToken` | `UserAndConsentUpdatingDto` | `UserDto` (vista profile) | `User`, `DataProcessingConsent`, `AccessLink` | `permitAll` |

Notas:
- `GET /users/{id}` tiene dos variantes por patron (`UUID` o `mobile`).
- En rutas con token URL, `id` corresponde al `urlId` de `AccessLink`.

## AccessLinksResource (`/access-links`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| POST | `/access-links` | `create` | `AccessLinkCreationDto` | `AccessLinkResponseDto` | `AccessLink`, `User` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/access-links/{id}` (`UUID`) | `read` | - | `AccessLinkResponseDto` | `AccessLink`, `User` | `ADMIN_MANAGER_OPERATOR` |
| DELETE | `/access-links/{id}` (`UUID`) | `delete` | - | - | `AccessLink` | `ADMIN` |
| GET | `/access-links` | `find` | `AccessLinkFindCriteria` (`@ModelAttribute`) | `List<AccessLinkResponseDto>` (vista summary) | `AccessLink`, `User` | `ADMIN_MANAGER_OPERATOR` |
| POST | `/access-links/{scope}/{id}/consume` (`id` base64url) | `consumeToken` | body `String` (token) | `AccessLinkResponseDto` | `AccessLink`, `User` | `ADMIN_MANAGER_OPERATOR_URL_TOKEN` |

Notas:
- `POST /access-links` devuelve, entre otros, `token` en la respuesta de creacion y no se podra recuperar de otra forma.
- `POST /access-links/{scope}/{id}/consume` recibe el token en el body (texto plano).

## DataProcessingConsentResource (`/consents`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| GET | `/consents/{id}` (`UUID`) | `read` | - | `DataProcessingConsentResponseDto` | `DataProcessingConsent` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/consents` | `find` | `DataProcessingConsentFindCriteria` (`@ModelAttribute`) | `List<DataProcessingConsentResponseDto>` (vista resumida) | `DataProcessingConsent` | `ADMIN_MANAGER_OPERATOR` |

## Contenido DTO y comparativa con Entity

### `UserDto` vs `User`

| Campo DTO | Tipo DTO | Campo Entity | Tipo Entity | Nota |
|---|---|---|---|---|
| `id` | `UUID` | `id` | `UUID` | DTO `READ_ONLY` |
| `mobile` | `String` | `mobile` | `String` | |
| `firstName` | `String` | `firstName` | `String` | |
| `familyName` | `String` | `familyName` | `String` | |
| `email` | `String` | `email` | `String` | |
| `identity` | `String` | `identity` | `String` | |
| `address` | `String` | `address` | `String` | |
| `city` | `String` | `city` | `String` | |
| `province` | `Province` | `province` | `Province` | |
| `postalCode` | `Integer` | `postalCode` | `Integer` | |
| `password` | `String` | `password` | `String` | DTO `WRITE_ONLY` |
| `role` | `Role` | `role` | `Role` | |
| `registrationDate` | `LocalDate` | `registrationDate` | `LocalDate` | DTO `READ_ONLY` |
| `active` | `Boolean` | `active` | `Boolean` | |

### `AccessLinkCreationDto` y `AccessLinkResponseDto` vs `AccessLink`

| Campo DTO | Tipo DTO | Campo Entity | Tipo Entity | Nota |
|---|---|---|---|---|
| `mobile` (`AccessLinkCreationDto`) | `String` | `user.mobile` | `String` | En entity se referencia via `user` (`@DBRef`) |
| `scope` (`AccessLinkCreationDto`) | `String` | `scope` | `String` | |
| `documentId` (`AccessLinkCreationDto`) | `UUID` | `documentId` | `UUID` | |
| `id` (`AccessLinkResponseDto`) | `UUID` | `id` | `UUID` | |
| `urlId` (`AccessLinkResponseDto`) | `String` | `urlId` | `String` | |
| `token` (`AccessLinkResponseDto`) | `String` | - | - | No se persiste en claro; en entity existe `tokenHash` |
| `fullName` (`AccessLinkResponseDto`) | `String` | derivado de `user` | `User` | Campo calculado (`user.fullName()`) |
| `createdAt` (`AccessLinkResponseDto`) | `LocalDateTime` | `createdAt` | `LocalDateTime` | |
| `lastUsedAt` (`AccessLinkResponseDto`) | `LocalDateTime` | `lastUsedAt` | `LocalDateTime` | |
| `expiresAt` (`AccessLinkResponseDto`) | `LocalDateTime` | `expiresAt` | `LocalDateTime` | |
| `remainingUses` (`AccessLinkResponseDto`) | `Integer` | `remainingUses` | `Integer` | |
| `scope` (`AccessLinkResponseDto`) | `String` | `scope` | `String` | |
| `documentId` (`AccessLinkResponseDto`) | `UUID` | `documentId` | `UUID` | |

### `DataProcessingConsentCreationDto` y `DataProcessingConsentResponseDto` vs `DataProcessingConsent`

| Campo DTO | Tipo DTO | Campo Entity | Tipo Entity | Nota |
|---|---|---|---|---|
| `dataProcessingAccepted` (`DataProcessingConsentCreationDto`) | `Boolean` | `dataProcessingAccepted` | `Boolean` | En creacion es obligatorio y `@AssertTrue` |
| `promotionsAccepted` (`DataProcessingConsentCreationDto`) | `Boolean` | `promotionsAccepted` | `Boolean` | |
| `id` (`DataProcessingConsentResponseDto`) | `UUID` | `id` | `UUID` | |
| `signatureAt` (`DataProcessingConsentResponseDto`) | `LocalDateTime` | `signatureAt` | `LocalDateTime` | |
| `signerFullName` (`DataProcessingConsentResponseDto`) | `String` | `signerFullName` | `String` | |
| `signerIdentity` (`DataProcessingConsentResponseDto`) | `String` | `signerIdentity` | `String` | |
| `mobile` (`DataProcessingConsentResponseDto`) | `String` | `mobile` | `String` | |
| `policyVersion` (`DataProcessingConsentResponseDto`) | `String` | `policyVersion` | `String` | |
| `signerEmail` (`DataProcessingConsentResponseDto`) | `String` | `signerEmail` | `String` | |
| `signatureToken` (`DataProcessingConsentResponseDto`) | `String` | `signatureToken` | `String` | |
| `deviceInfo` (`DataProcessingConsentResponseDto`) | `DeviceInfo` | `deviceInfo` | `DeviceInfo` | |
| `dataProcessingAccepted` (`DataProcessingConsentResponseDto`) | `Boolean` | `dataProcessingAccepted` | `Boolean` | |
| `promotionsAccepted` (`DataProcessingConsentResponseDto`) | `Boolean` | `promotionsAccepted` | `Boolean` | |
| - | - | `signer` | `User` (`@DBRef`) | Solo en entity |

### `UserAndConsentUpdatingDto` (compuesto)

| Campo DTO | Tipo DTO | Mapeo en Entities |
|---|---|---|
| `user` | `UserDto` | `User` |
| `dataProcessingConsentCreation` | `DataProcessingConsentCreationDto` | `DataProcessingConsent` |

### `ProvincesResponseDto` vs `Province`

| Campo DTO | Tipo DTO | Origen |
|---|---|---|
| `provinces` | `List<String>` | Valores de `Province.values().name()` |
