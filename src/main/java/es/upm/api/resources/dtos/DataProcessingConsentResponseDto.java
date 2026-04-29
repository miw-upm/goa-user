package es.upm.api.resources.dtos;

import es.upm.api.data.entities.DataProcessingConsent;
import es.upm.miw.device.DeviceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataProcessingConsentResponseDto {
    private UUID id;
    private LocalDateTime signatureAt;
    private String signerFullName;
    private String signerIdentity;
    private String mobile;
    private String policyVersion;
    private String signerEmail;
    private String signatureToken;
    private DeviceInfo deviceInfo;
    private Boolean dataProcessingAccepted;
    private Boolean promotionsAccepted;

    public DataProcessingConsentResponseDto(DataProcessingConsent consent) {
        BeanUtils.copyProperties(consent, this);
    }

    public DataProcessingConsentResponseDto ofMobileFullNameSignatureAt() {
        return DataProcessingConsentResponseDto.builder()
                .id(this.getId())
                .mobile(this.getMobile())
                .signerFullName(this.getSignerFullName())
                .signatureAt(this.getSignatureAt())
                .build();
    }
}
