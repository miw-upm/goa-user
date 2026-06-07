package es.upm.api.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Objects;
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
    private String email;
    private String identity;
    private String address;
    private String city;
    private Province province;
    private Integer postalCode;
    private String password;
    private Role role;
    private LocalDate registrationDate;
    private Boolean active;

    public String fullName() {
        return (Objects.toString(this.firstName, "") + " " + Objects.toString(this.familyName, "")).trim();
    }

    public boolean isBillable() {
        return this.familyName != null && !this.familyName.isBlank()
                && this.email != null && !this.email.isBlank()
                && this.identity != null && !this.identity.isBlank()
                && this.address != null && !this.address.isBlank()
                && this.city != null && !this.city.isBlank()
                && this.province != null
                && this.postalCode != null;
    }
}
