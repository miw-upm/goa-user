package es.upm.api.resources.dtos;

import java.time.LocalDateTime;

public record ApplicationInfoDto(String version, LocalDateTime timestamp) {
}

