# Migración 2026-05 · Cifrado de datos sensibles, hash de consentimientos y rediseño de AccessLink

## Resumen

Migración única (one-shot) ejecutada para:

1. **Cifrar campos sensibles de `User`** (`identity`, `email`, `address`) que hasta entonces se almacenaban en texto plano.
2. **Hashear campos identificativos de `DataProcessingConsent`** (`signerIdentity`, `signerEmail`, `signatureToken` y `deviceInfo.ipAddress`) para cumplir con el principio de minimización de datos personales.
3. **Rediseñar `AccessLink`**: pasar de un identificador único en texto plano a un esquema con `_id` interno (UUID), `urlId` público y `token` derivado por hash, junto con el renombrado de algunos campos.

Tras la ejecución correcta, las clases `*Migration` se eliminan del código fuente. Este documento conserva el código original para fines de auditoría y trazabilidad.

**Fecha de ejecución**: 5 de mayo de 2026, 19:00.

---

## 1. Cifrado de datos sensibles del usuario (`UserMigration`)

### Cambios en el modelo

Los siguientes campos de `User` pasan de almacenarse en texto plano a almacenarse cifrados (cifrado simétrico reversible vía `EncryptionService`):

| Campo            | Antes       | Después        |
|------------------|-------------|----------------|
| `identity` (NIF) | texto plano | cifrado        |
| `email`          | texto plano | cifrado        |
| `address`        | texto plano | cifrado        |

### Comportamiento de la migración

Para cada usuario, los tres campos se procesan con `migrateEncryptedField`, que es **idempotente**: si el valor ya estaba cifrado (`encryptionService.isEncrypted(value)` devuelve `true`), se deja como está; en caso contrario se cifra. Esto permite reejecutar el `ApplicationRunner` en arranques sucesivos sin riesgo de doble cifrado.

Solo se persiste el documento si al menos uno de los campos cambia.

### Impacto en consultas

Tras la migración, cualquier búsqueda directa por `identity`, `email` o `address` deja de funcionar con el valor en claro. Las queries que filtraban por estos campos se adaptaron para cifrar previamente el criterio de búsqueda antes de consultar Mongo.

---

## 2. Hash de campos en consentimientos (`DataProcessingConsentMigration`)

### Cambios en el modelo

Los siguientes campos de `DataProcessingConsent` pasan a almacenarse como hash irreversible (BCrypt vía `HashService`):

| Campo                       | Antes       | Después |
|-----------------------------|-------------|---------|
| `signerIdentity`            | texto plano | hash    |
| `signerEmail`               | texto plano | hash    |
| `signatureToken`            | texto plano | hash    |
| `deviceInfo.ipAddress`      | texto plano | hash    |

### Comportamiento de la migración

Para cada consentimiento, los cuatro campos se procesan con `migrateHashedField`, que es **idempotente**: si el valor ya estaba hasheado (`hashService.isHashed(value)` devuelve `true`, lo que se detecta por el prefijo `{bcrypt}`), se deja como está; en caso contrario se hashea. Esto permite reejecutar la migración con seguridad.

Solo se persiste el consentimiento si alguno de los campos cambia respecto al valor anterior.

### Impacto en consultas

El hash es irreversible, así que estos campos solo pueden usarse para comparar igualdad mediante `passwordEncoder.matches(raw, hashed)`. No se pueden recuperar los valores originales desde el lado del backend.

---

## 3. Rediseño de `AccessLink` (`AccessLinkMigration`)

### Cambios en el modelo

| Campo antiguo            | Campo nuevo    | Transformación                                |
|--------------------------|----------------|-----------------------------------------------|
| `_id` (String)           | `_id` (UUID)   | nuevo UUID generado en la migración           |
| —                        | `urlId`        | recibe el valor del antiguo `_id`             |
| —                        | `token`        | hash del antiguo `_id` (vía `HashService`)    |
| `lastUsedForUpdatedAt`   | `lastUsedAt`   | renombrado, mismo valor                       |
| `document`               | `documentId`   | renombrado, mismo valor                       |
| `user`, `createdAt`, `expiresAt`, `remainingUses`, `scope` | (sin cambios) | copiados tal cual            |

### Por qué este rediseño

El `_id` antiguo se usaba a la vez como identificador interno y como parte de la URL pública enviada al cliente. Separarlos permite:

- **`urlId`**: identificador público (lo que el cliente conoce y manda en la URL).
- **`token`**: secreto derivado del `urlId` mediante hash; el backend lo recalcula y compara, evitando que un atacante con solo el `urlId` pueda hacer el lookup directamente en la BD.
- **`_id` (UUID)**: identificador interno opaco, sin relación con la URL ni con el secreto.

### Comportamiento de la migración

Trabaja a nivel de `MongoCollection` directamente (no vía repositorio) porque cambia el tipo del `_id`, lo que el repositorio Spring Data no permite hacer en un `save`. Por cada documento con `_id` de tipo `String`:

1. Construye un nuevo documento con la nueva estructura.
2. Lo inserta.
3. Borra el documento original.

Los documentos cuyo `_id` ya sea `UUID` se ignoran (`continue`), lo que hace la migración **idempotente**.

---

## Plan de despliegue y rollback

- Las tres clases estaban anotadas con `@Profile("prod")` y se ejecutaban una sola vez al arrancar el contenedor tras el despliegue.
- Antes de la ejecución se realizó un **dump completo de la base de datos** como punto de rollback. No se diseñó un proceso de reversión automática porque:
  - Los hashes son irreversibles por definición.
  - El cifrado es reversible, pero la complejidad de revertir solo parte del esquema no compensaba frente a un restore desde dump.
- Tras verificar el resultado correcto, se eliminaron las clases.

---

## Código original (eliminado)

A continuación se conserva el código completo de las tres clases.

### `AccessLinkMigration.java`

```java
package es.upm.api.infrastructure.migrations;

import com.mongodb.client.MongoCollection;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.infrastructure.support.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
@Profile("prod")
public class AccessLinkMigration implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final HashService hashService;

    @Override
    public void run(ApplicationArguments args) {
        int migratedAccessLinks = 0;
        String collectionName = this.mongoTemplate.getCollectionName(AccessLink.class);
        MongoCollection<Document> collection = this.mongoTemplate.getCollection(collectionName);
        for (Document accessLink : collection.find()) {
            Object id = accessLink.get("_id");
            if (!(id instanceof String oldId)) {
                continue;
            }
            Document newAccessLink = new Document();
            newAccessLink.put("_id", UUID.randomUUID());
            newAccessLink.put("urlId", oldId);
            newAccessLink.put("token", this.hashService.hash(oldId));
            newAccessLink.put("user", accessLink.get("user"));
            newAccessLink.put("createdAt", accessLink.get("createdAt"));
            newAccessLink.put("lastUsedAt", accessLink.get("lastUsedForUpdatedAt"));
            newAccessLink.put("expiresAt", accessLink.get("expiresAt"));
            newAccessLink.put("remainingUses", accessLink.get("remainingUses"));
            newAccessLink.put("scope", accessLink.get("scope"));
            newAccessLink.put("documentId", accessLink.get("document"));
            collection.insertOne(newAccessLink);
            collection.deleteOne(new Document("_id", oldId));
            migratedAccessLinks++;
        }
        log.warn("AccessLink migration finished. Migrated access links: {}", migratedAccessLinks);
    }
}
```

### `DataProcessingConsentMigration.java`

```java
package es.upm.api.infrastructure.migrations;

import es.upm.api.data.daos.DataProcessingConsentRepository;
import es.upm.api.data.entities.DataProcessingConsent;
import es.upm.api.infrastructure.support.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Log4j2
@Profile("prod")
public class DataProcessingConsentMigration implements ApplicationRunner {

    private final HashService hashService;
    private final DataProcessingConsentRepository dataProcessingConsentRepository;

    @Override
    public void run(ApplicationArguments args) {
        int migratedConsents = 0;
        for (DataProcessingConsent consent : dataProcessingConsentRepository.findAll()) {
            if (migrateConsent(consent)) {
                dataProcessingConsentRepository.save(consent);
                migratedConsents++;
            }
        }
        log.warn("Data processing consent migration finished. Migrated consents: {}", migratedConsents);
    }

    private boolean migrateConsent(DataProcessingConsent consent) {
        boolean changed = false;
        String signerIdentity = this.migrateHashedField(consent.getSignerIdentity());
        if (!Objects.equals(consent.getSignerIdentity(), signerIdentity)) {
            consent.setSignerIdentity(signerIdentity);
            changed = true;
        }
        String signerEmail = this.migrateHashedField(consent.getSignerEmail());
        if (!Objects.equals(consent.getSignerEmail(), signerEmail)) {
            consent.setSignerEmail(signerEmail);
            changed = true;
        }
        String signatureToken = this.migrateHashedField(consent.getSignatureToken());
        if (!Objects.equals(consent.getSignatureToken(), signatureToken)) {
            consent.setSignatureToken(signatureToken);
            changed = true;
        }
        if (consent.getDeviceInfo() != null) {
            String ipAddress = this.migrateHashedField(consent.getDeviceInfo().getIpAddress());
            if (!Objects.equals(consent.getDeviceInfo().getIpAddress(), ipAddress)) {
                consent.getDeviceInfo().setIpAddress(ipAddress);
                changed = true;
            }
        }
        return changed;
    }

    private String migrateHashedField(String value) {
        if (this.hashService.isHashed(value)) {
            return value;
        }
        return this.hashService.hash(value);
    }
}
```

### `UserMigration.java`

```java
package es.upm.api.infrastructure.migrations;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.User;
import es.upm.api.infrastructure.support.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Log4j2
@Profile("prod")
public class UserMigration implements ApplicationRunner {
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int migratedUsers = 0;
        for (User user : userRepository.findAll()) {
            if (migrateUser(user)) {
                userRepository.save(user);
                migratedUsers++;
            }
        }
        log.warn("User sensitive fields migration finished. Migrated users: {}", migratedUsers);
    }

    private boolean migrateUser(User user) {
        boolean changed = false;
        String identity = this.migrateEncryptedField(user.getIdentity());
        if (!Objects.equals(user.getIdentity(), identity)) {
            user.setIdentity(identity);
            changed = true;
        }
        String email = this.migrateEncryptedField(user.getEmail());
        if (!Objects.equals(user.getEmail(), email)) {
            user.setEmail(email);
            changed = true;
        }
        String address = this.migrateEncryptedField(user.getAddress());
        if (!Objects.equals(user.getAddress(), address)) {
            user.setAddress(address);
            changed = true;
        }
        return changed;
    }

    private String migrateEncryptedField(String value) {
        if (encryptionService.isEncrypted(value)) {
            return value;
        }
        return encryptionService.encrypt(value);
    }
}
```
