package es.upm.api.services;

import es.upm.api.configurations.CurrentUser;
import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.api.services.criteria.UserFindCriteria;
import es.upm.api.services.emailoutport.EmailPort;
import es.upm.api.services.utils.ProfileUpdatedEmailTemplateService;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.exception.*;
import es.upm.miw.uuid.UUIDBase64;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static es.upm.api.data.entities.Role.CUSTOMER;

@RequiredArgsConstructor
@Service
public class UserService {
    public static final String SCOPE_EDIT_PROFILE = "edit-profile";

    private final AccessLinkService accessLinkService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessLinkRepository accessLinkRepository;
    private final EmailPort emailPort;
    private final ProfileUpdatedEmailTemplateService profileUpdatedEmailTemplateService;
    private final DataProcessingConsentService dataProcessingConsentService;
    private final CurrentUser currentUser;

    public void create(User user) {
        this.validateAuthorizedRole(user.getRole());
        this.assertNoExistByMobile(user.getMobile());
        this.assertNoExistByEmail(user.getEmail());
        this.assertNoExistByDni(user.getIdentity());
        user.setId(UUID.randomUUID());
        if (Objects.isNull(user.getPassword())) {
            user.setPassword(UUIDBase64.BASIC.encode());
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(LocalDate.now());
        this.userRepository.save(user);
    }

    public User updateByMobile(String mobile, User user) {
        this.validateAuthorizedRole(user.getRole());
        return this.updateUser(mobile, user);
    }

    public User updateByMobileWithToken(String mobile, String token, User user, boolean dataProcessingAccepted,
                                        boolean promotionsAccepted, DeviceInfo deviceInfo) {
        this.accessLinkService.use(token,mobile,SCOPE_EDIT_PROFILE);
        User existingUser = this.readByMobile(mobile);
        if (!CUSTOMER.equals(existingUser.getRole())) {
            throw new ForbiddenException("Forbidden. Only CUSTOMER allowed. Role:" + existingUser.getRole() + "Mobile: " + mobile);
        }
        user.setRole(CUSTOMER);
        boolean profileChanged = !EqualsBuilder.reflectionEquals(existingUser, user,
                "id", "password", "role", "registrationDate", "active");
        User userDB = this.updateUser(mobile, user);
        this.dataProcessingConsentService.create(userDB, token, dataProcessingAccepted, promotionsAccepted, deviceInfo);
        if (profileChanged) {
            try {
                this.emailPort.sendHtml(
                        this.profileUpdatedEmailTemplateService.buildHtmlEmail(
                                userDB.getEmail(),
                                userDB.getFirstName(),
                                userDB.getMobile(),
                                deviceInfo
                        )
                );
            } catch (FeignException.BadRequest e) {
                throw new BadGatewayException("Error de email: (" + userDB.getEmail() + ")", e.getCause());
            } catch (Exception e) {
                throw new BadGatewayException("Error del host de email", e.getCause());
            }
        }
        return userDB;
    }

    private User updateUser(String mobile, User user) {
        User existing = this.readByMobile(mobile);
        if (!mobile.equals(user.getMobile())) {
            this.assertNoExistByMobile(user.getMobile());
        }
        if (!Objects.equals(existing.getEmail(), user.getEmail())) {
            this.assertNoExistByEmail(user.getEmail());
        }
        if (!Objects.equals(existing.getIdentity(), user.getIdentity())) {
            this.assertNoExistByDni(user.getIdentity());
        }
        if (!Objects.isNull(user.getPassword())) {
            existing.setPassword(this.passwordEncoder.encode(user.getPassword()));
        }
        BeanUtils.copyProperties(user, existing, "id", "password", "registrationDate", "active");
        return this.userRepository.save(existing);
    }

    private void validateAuthorizedRole(Role role) {
        List<Role> roles = this.validRoles();
        if (!roles.contains(role)) {
            throw new ForbiddenException("Insufficient role to update this user, role: " + role);
        }
    }

    private List<Role> validRoles() {
        Role authRole = this.currentUser.getRole();
        return switch (authRole) {
            case ADMIN -> List.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR, CUSTOMER);
            case MANAGER -> List.of(Role.MANAGER, Role.OPERATOR, CUSTOMER);
            case OPERATOR, CUSTOMER, URL_TOKEN -> List.of(CUSTOMER);
            default -> List.of();
        };
    }

    public User read(UUID id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + id));
    }

    public User readByMobile(String mobile) {
        return this.userRepository.findByMobile(mobile)
                .orElseThrow(() -> new NotFoundException("The mobile don't exists: " + mobile));
    }

    public User readByMobileWithToken(String mobile, String token) {
        this.accessLinkService.use(token,mobile,SCOPE_EDIT_PROFILE);
        return this.readByMobile(mobile);
    }

    private void assertNoExistByEmail(String email) {
        if (email != null && this.userRepository.existsByEmail(email)) {
            throw new ConflictException("The email already exists: " + email);
        }
    }

    private void assertNoExistByMobile(String mobile) {
        if (this.userRepository.existsByMobile(mobile)) {
            throw new ConflictException("The mobile already exists: " + mobile);
        }
    }

    private void assertNoExistByDni(String dni) {
        if (dni != null && this.userRepository.existsByIdentity(dni)) {
            throw new ConflictException("The dni already exists: " + dni);
        }
    }

    public Stream<User> find(UserFindCriteria criteria) {
        return this.restrictToCurrentCustomer(this.query(criteria));
    }

    private Stream<User> query(UserFindCriteria criteria) {
        if (criteria.all()) {
            return this.userRepository.findByRoleIn(this.validRoles()).stream();
        }
        if (criteria.getAttribute() != null) {
            return this.userRepository.findByAll(criteria.getAttribute(), List.of(CUSTOMER)).stream();
        }
        return this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                criteria.getMobile(), criteria.getFirstName(), criteria.getFamilyName(),
                criteria.getEmail(), criteria.getIdentity(), this.validRoles()
        ).stream();
    }

    private Stream<User> restrictToCurrentCustomer(Stream<User> users) {
        if (!this.currentUser.isCustomer()) {
            return users;
        }
        return users.filter(user -> user.getMobile().equals(this.currentUser.mobile()));
    }

}
