package es.upm.api.resources.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataProcessingConsentCreationDto {
    @NotNull
    @AssertTrue
    private Boolean dataProcessingAccepted;
    private Boolean promotionsAccepted;
}
