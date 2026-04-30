package es.upm.api.resources.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAndConsentUpdatingDto {
    @Valid
    @NotNull
    private UserDto user;

    @Valid
    @NotNull
    private DataProcessingConsentCreationDto dataProcessingConsentCreation;
}