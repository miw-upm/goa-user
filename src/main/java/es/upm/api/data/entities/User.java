package es.upm.api.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "goaUser") // conflict with user table
public class User {
    @Id
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;
    @Column(unique = true, nullable = false)
    private String mobile;
    private String firstName;
    private String familyName;
    @Column(unique = true)
    private String email;
    private DocumentType documentType;
    @Column(unique = true)
    private String identity;
    private String address;
    private Integer postcode;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDate registrationDate;
    private Boolean active;
}
