package es.upm.api.resources.dtos;

import es.upm.api.services.DataProcessingConsentCreation;
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
public class UserUpdateWithConsentDto {
    @Valid
    @NotNull
    private UserDto user;

    @Valid
    @NotNull
    private DataProcessingConsentCreation consent;
}