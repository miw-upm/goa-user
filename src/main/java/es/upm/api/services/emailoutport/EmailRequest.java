package es.upm.api.services.emailoutport;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    @NotBlank
    public String to;
    @NotBlank
    public String subject;
    @NotBlank
    public String body;
}
