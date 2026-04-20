package es.upm.api.data.entities;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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
    private User signer;
    private String signerFullName;
    private String signerIdentity;
    private String signerEmail;
    private String deviceInfo;
    private String signatureToken;
    private String policyVersion;
    @NotNull
    @AssertTrue
    private Boolean dataProcessingAccepted;
    private Boolean promotionsAccepted;
}
