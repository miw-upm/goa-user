package es.upm.api.resources.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import es.upm.api.data.entities.Province;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.miw.security.Validations;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @JsonProperty(access = Access.READ_ONLY)
    private UUID id;
    @NotNull
    @NotBlank
    @Pattern(regexp = Validations.MOBILE_RX)
    private String mobile;
    @NotNull
    @NotBlank
    private String firstName;
    private String familyName;
    private String email;
    private String identity;
    private String address;
    private String city;
    private Province province;
    private Integer postalCode;
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;
    private Role role;
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDate registrationDate;
    private Boolean active;
    @JsonProperty(access = Access.READ_ONLY)
    private Boolean billable;

    public UserDto(User user) {
        BeanUtils.copyProperties(user, this);
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

    public static UserDto of(User user, boolean full) {
        UserDto dto = new UserDto(user);
        return full ? dto : dto.ofSummary();
    }

    public void doDefault() {
        if (Objects.isNull(role)) {
            this.role = Role.CUSTOMER;
        }
        if (Objects.isNull(active)) {
            this.active = true;
        }
    }

    public UserDto ofSummary() {
        boolean result = this.familyName != null && !this.familyName.isBlank()
                && this.email != null && !this.email.isBlank()
                && this.identity != null && !this.identity.isBlank()
                && this.address != null && !this.address.isBlank()
                && this.city != null && !this.city.isBlank()
                && this.province != null
                && this.postalCode != null;
        return UserDto.builder()
                .id(this.getId())
                .mobile(this.getMobile())
                .firstName(this.getFirstName())
                .familyName(this.getFamilyName())
                .email(this.getEmail())
                .billable(result)
                .build();
    }

    public UserDto ofProfile() {
        return this.ofSummary().toBuilder()
                .address(this.getAddress())
                .city(this.getCity())
                .province(this.getProvince())
                .postalCode(this.getPostalCode())
                .identity(this.getIdentity())
                .build();
    }

    public User toDomain() {
        User user = new User();
        BeanUtils.copyProperties(this, user);
        return user;
    }
}
