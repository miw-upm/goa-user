package es.upm.api.resources.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLinkCreationDto {
    @NotNull
    @NotBlank
    private String mobile;
    @NotNull
    @NotBlank
    private String scope;
}
