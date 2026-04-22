## [Máster en Ingeniería Web por la Universidad Politécnica de Madrid (miw-upm)](http://miw.etsisi.upm.es)

## Back-end con Tecnologías de Código Abierto (BETCA).

> Este proyecto es un apoyo docente de la asignatura y contiene un proyecto completo con Spring.

Es un ejemplo de un API Rest completo, basado en Spring Boot, con una arquitectura de tres capas, y almacenamiento en
bases de datos con JPA soportado por Hibernate y Postgres.
La seguridad esta basada en OAuth2 y OpenId Connect, desarrollando ambos procesos en el API

### Estado del código

[![CI goa-user](https://github.com/miw-upm/goa-user/actions/workflows/ci.yml/badge.svg)](https://github.com/miw-upm/goa-user/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=miw-upm-github_goa-user&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=miw-upm-github_goa-user)
[![AWS broken](https://goa.miwump.es/api/goa-user/system/version-badge)](https://goa.miwump.es/api/goa-user/system)

## Tecnologías necesarias

`Java` `Maven` `GitHub` `Spring-Boot` `Sonarcloud` `JPA` `MongoDB` `Docker` `OAuth2` `OpenID Connect`

## Configuración de Maven para GitHub Packages

```bash
export GITHUB_READ_TOKEN=valor_del_token
mkdir -p ~/.m2 && echo "<settings><servers><server><id>github</id><username>x</username>\
<password>$GITHUB_READ_TOKEN</password></server></servers></settings>" > ~/.m2/settings.xml
```

> Sustituye `valor_del_token` por el token proporcionado y ejecuta ambos comandos en el terminal.

### :gear: Instalación del proyecto

1. Clonar el repositorio en tu equipo, **mediante consola**:

```sh
> cd <folder path>
> git clone https://github.com/miw-upm/goa-user
```

2. Importar el proyecto mediante **IntelliJ IDEA**
    * **Open**, y seleccionar la carpeta del proyecto.

### :gear: Ejecución en local

* Ejecutar en consola del proyecto el siguiente comando de Docker:

```sh
> docker compose up --build -d
```

* Necesita de una bases de datos: **url:** `mongodb://mongo:mongo@localhost:27017/goauserdb?authSource=admin`, *
  *username:**`postgres`.

* Se aporta un fichero `docker-compose-db.yml`que monta el motor de BD sobre Docker:  MongoDB.

```sh
> docker compose -f docker-compose-db.yml -p databases up -d
```

* Cliente Web: `http://localhost:8081/swagger-ui.html`

