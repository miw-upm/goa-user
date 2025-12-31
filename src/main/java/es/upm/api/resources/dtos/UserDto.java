package es.upm.api.resources.dtos;

import es.upm.api.data.entities.DocumentType;
import es.upm.api.data.entities.Province;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.api.resources.dtos.validations.Validations;
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

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
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
    private DocumentType documentType;
    private String identity;
    private String address;
    private String city;
    private Province province;
    private Integer postalCode;
    private String password;
    private Role role;
    private LocalDate registrationDate;
    private Boolean active;

    public UserDto(User user) {
        BeanUtils.copyProperties(user, this);
        this.password = null;
    }

    public void doDefault() {
        if (Objects.isNull(role)) {
            this.role = Role.CUSTOMER;
        }
        if (Objects.isNull(active)) {
            this.active = true;
        }
    }

    public UserDto ofMobileFirstNameFamilyNameEmail() {
        return UserDto.builder()
                .id(this.getId())
                .mobile(this.getMobile())
                .firstName(this.getFirstName())
                .familyName(this.getFamilyName())
                .email(this.getEmail()).build();
    }

    public UserDto ofAllBasic() {
        return UserDto.builder()
                .id(this.getId())
                .mobile(this.getMobile())
                .firstName(this.getFirstName())
                .familyName(this.getFamilyName())
                .email(this.getEmail())
                .documentType(this.getDocumentType())
                .identity(this.getIdentity())
                .address(this.getAddress())
                .city(this.getCity())
                .province(this.getProvince())
                .postalCode(this.getPostalCode())
                .build();
    }

    public User toUser() {
        User user = new User();
        BeanUtils.copyProperties(this, user);
        return user;
    }
}
