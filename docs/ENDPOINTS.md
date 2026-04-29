# Endpoints API (`goa-user`)

Listado de endpoints expuestos por los resources actuales.

## SystemResource (`/system`)

| Metodo | Ruta | Metodo Java | Seguridad |
|---|---|---|---|
| GET | `/system` | `applicationInfo` | Publica |
| GET | `/system/version-badge` | `generateBadge` | Publica |

## UserResource (`/users`)

| Metodo | Ruta | Metodo Java | Seguridad |
|---|---|---|---|
| POST | `/users` | `create` | `permitAll` |
| GET | `/users/{id}` (`UUID`) | `readById` | `ADMIN_MANAGER_OPERATOR_URL_TOKEN` |
| GET | `/users/{id}` (`mobile`) | `readByMobile` | `ADMIN_MANAGER_OPERATOR_URL_TOKEN` |
| PUT | `/users/{id}` (`UUID`) | `update` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/users` | `find` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/users/provinces` | `findProvinces` | `permitAll` |
| GET | `/users/{scope}/{id}/{token}` | `readByUrlIdWithToken` | `permitAll` |
| PUT | `/users/{scope}/{id}/{token}` | `updateByUrlIdWithToken` | `permitAll` |

Notas:
- `GET /users/{id}` tiene dos variantes por patron (`UUID` o `mobile`).
- En rutas con token URL: `id` corresponde al `urlId` del access-link.

## AccessLinksResource (`/access-links`)

| Metodo | Ruta | Metodo Java | Seguridad |
|---|---|---|---|
| POST | `/access-links` | `create` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/access-links/{id}` (`UUID`) | `read` | `ADMIN_MANAGER_OPERATOR` |
| DELETE | `/access-links/{id}` (`UUID`) | `delete` | `ADMIN` |
| GET | `/access-links` | `find` | `ADMIN_MANAGER_OPERATOR` |
| POST | `/access-links/{scope}/{id}/consume` (`id` base64url) | `consumeToken` | `ADMIN_MANAGER_OPERATOR_URL_TOKEN` |

Notas:
- `POST /access-links` devuelve `token` en la respuesta de creacion.
- `POST /access-links/{scope}/{id}/consume` recibe el token en el body (texto plano).

## DataProcessingConsentResource (`/consents`)

| Metodo | Ruta | Metodo Java | Seguridad |
|---|---|---|---|
| GET | `/consents/{id}` (`UUID`) | `read` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/consents` | `find` | `ADMIN_MANAGER_OPERATOR` |

