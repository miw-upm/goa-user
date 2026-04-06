package es.upm.api.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class User {
    @Id
    private UUID id;
    @Indexed(unique = true)
    private String mobile;
    private String firstName;
    private String familyName;
    @Indexed(unique = true)
    private String email;
    private DocumentType documentType;
    @Indexed(unique = true)
    private String identity;
    private String address;
    private String city;
    private Province province;
    private Integer postalCode;
    private String password;
    private Role role;
    private LocalDate registrationDate;
    private Boolean active;
}
