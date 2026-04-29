package es.upm.api.data.entities;

import es.upm.miw.device.DeviceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class DataProcessingConsent {
    @Id
    private UUID id;
    private LocalDateTime signatureAt;
    @DBRef
    private User signer;
    private String signerFullName;
    private String signerIdentity;
    private String mobile;
    private String policyVersion;
    private String signerEmail;
    private String signatureToken;
    private DeviceInfo deviceInfo;
    private Boolean dataProcessingAccepted;
    private Boolean promotionsAccepted;
}
