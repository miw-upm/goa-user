package es.upm.api.resources.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLinkCreationDto {
    @NotNull
    @NotBlank
    private String scope;
    @NotNull
    @NotBlank
    private String mobile;
    private UUID documentId;
    @Future
    private LocalDateTime expiresAt;
}
