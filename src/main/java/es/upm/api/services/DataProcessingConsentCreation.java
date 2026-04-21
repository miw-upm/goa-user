package es.upm.api.services;

import es.upm.miw.device.DeviceInfo;
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
public class DataProcessingConsentCreation {
    @NotNull
    @AssertTrue
    private Boolean dataProcessingAccepted;
    private Boolean promotionsAccepted;
}
