package es.upm.api.services;

import es.upm.api.configurations.CurrentUser;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.api.infrastructure.clients.email.GoaSupportClient;
import es.upm.api.infrastructure.support.EncryptionService;
import es.upm.api.infrastructure.support.HashService;
import es.upm.api.infrastructure.support.ProfileUpdatedEmailTemplateService;
import es.upm.api.services.criteria.UserFindCriteria;
import es.upm.miw.base64url.Base64UrlGenerator;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.exception.ClientBusinessException;
import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static es.upm.api.data.entities.Role.CUSTOMER;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {
    public static final String SCOPE_EDIT_PROFILE = "edit-profile";

    private final AccessLinkService accessLinkService;
    private final UserRepository userRepository;
    private final GoaSupportClient goaSupportClient;
    private final ProfileUpdatedEmailTemplateService profileUpdatedEmailTemplateService;
    private final DataProcessingConsentService dataProcessingConsentService;
    private final CurrentUser currentUser;
    private final EncryptionService encryptionService;
    private final HashService hashService;

    public void create(User user) {
        this.validateAuthorizedRole(user.getRole());
        this.assertNoExistByMobile(user.getMobile());
        user.setId(UUID.randomUUID());
        if (Objects.isNull(user.getPassword())) {
            user.setPassword(Base64UrlGenerator.encode());
        }
        user.setPassword(this.hashService.hash(user.getPassword()));
        user.setRegistrationDate(LocalDate.now());
        this.encryptSensitiveFields(user);
        this.userRepository.save(user);
    }

    public User update(UUID id, User user) {
        this.validateAuthorizedRole(user.getRole());
        return this.updateUser(id, user);
    }

    public User updateByUrlIdWithToken(String scope, String urlId, String token, User user, boolean dataProcessingAccepted,
                                       boolean promotionsAccepted, DeviceInfo deviceInfo) {
        User retrieverUser = this.decryptSensitiveFields(this.accessLinkService.consumeToken(scope, urlId, token).getUser());
        if (!CUSTOMER.equals(retrieverUser.getRole())) {
            throw new ForbiddenException("Forbidden. Only CUSTOMER allowed. Role:" + retrieverUser.getRole());
        }
        user.setRole(CUSTOMER);
        boolean profileChanged = !EqualsBuilder.reflectionEquals(retrieverUser, user,
                "id", "password", "role", "registrationDate", "active");
        User dbUser = this.updateUser(retrieverUser.getId(), user);
        this.dataProcessingConsentService.create(dbUser, token, dataProcessingAccepted, promotionsAccepted, deviceInfo);
        if (profileChanged) {
            this.sendEmail(deviceInfo, dbUser);
        }
        return dbUser;
    }

    private void sendEmail(DeviceInfo deviceInfo, User dbUser) {
        try {
            this.goaSupportClient.sendHtml(
                    this.profileUpdatedEmailTemplateService.buildHtmlEmail(
                            dbUser.getEmail(),
                            dbUser.getFirstName(),
                            dbUser.getMobile(),
                            deviceInfo
                    )
            );
        } catch (Exception e) {
            log.error("Could not send notification email to {}", dbUser.getEmail(), e);
        }
    }

    private User updateUser(UUID id, User user) {
        User existing = this.readById(id);
        if (!existing.getMobile().equals(user.getMobile())) {
            this.assertNoExistByMobile(user.getMobile());
        }
        if (!Objects.isNull(user.getPassword())) {
            existing.setPassword(this.hashService.hash(user.getPassword()));
        }
        BeanUtils.copyProperties(user, existing, "id", "password", "registrationDate");
        this.encryptSensitiveFields(existing);
        return this.decryptSensitiveFields(this.userRepository.save(existing));
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

    public User readById(UUID id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + id));
        return this.decryptSensitiveFields(user);
    }

    public User readByMobile(String mobile) {
        User user = this.userRepository.findByMobile(mobile)
                .orElseThrow(() -> new NotFoundException("The mobile don't exists: " + mobile));
        return this.decryptSensitiveFields(user);
    }

    public User readByUrlIdWithToken(String scope, String urlId, String token) {
        return this.decryptSensitiveFields(this.accessLinkService.consumeToken(scope, urlId, token).getUser());
    }

    private void assertNoExistByMobile(String mobile) {
        if (this.userRepository.existsByMobile(mobile)) {
            throw new ClientBusinessException("El móvil ya existe: " + mobile);
        }
    }

    public Stream<User> find(UserFindCriteria criteria) {
        Boolean billable = criteria.getBillable();
        return this.restrictToCurrentCustomer(this.query(criteria))
                .map(this::decryptSensitiveFields)
                .filter(user -> {
                    if (billable == null) {
                        return true;
                    }
                    return billable.equals(user.isBillable());
                });
    }

    public Stream<User> findAllFull() {
        return this.userRepository.findAll().stream()
                .map(this::decryptSensitiveFields);
    }

    private Stream<User> query(UserFindCriteria criteria) {
        if (criteria.all()) {
            return this.userRepository.findByRoleIn(this.validRoles()).stream();
        }
        return this.userRepository.findUsers(criteria.getCustomer(), criteria.getActive(), this.validRoles()).stream();
    }

    private Stream<User> restrictToCurrentCustomer(Stream<User> users) {
        if (!this.currentUser.isCustomer()) {
            return users;
        }
        return users.filter(user -> user.getMobile().equals(this.currentUser.mobile()));
    }

    private void encryptSensitiveFields(User user) {
        user.setIdentity(this.encryptionService.encrypt(user.getIdentity()));
        user.setEmail(this.encryptionService.encrypt(user.getEmail()));
        user.setAddress(this.encryptionService.encrypt(user.getAddress()));
    }

    private User decryptSensitiveFields(User user) {
        user.setIdentity(this.encryptionService.decrypt(user.getIdentity()));
        user.setEmail(this.encryptionService.decrypt(user.getEmail()));
        user.setAddress(this.encryptionService.decrypt(user.getAddress()));
        return user;
    }
}
