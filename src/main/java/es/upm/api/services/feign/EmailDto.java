package es.upm.api.services.feign;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {
    @NotBlank
    public String to;
    @NotBlank
    public String subject;
    @NotBlank
    public String body;
}
