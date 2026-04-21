package es.upm.api.services;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.api.resources.dtos.DataProcessingConsentCreationDto;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.exception.BadRequestException;
import es.upm.miw.exception.ConflictException;
import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.NotFoundException;
import es.upm.miw.uuid.UUIDBase64;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static es.upm.api.data.entities.Role.CUSTOMER;

@Service
public class UserService {
    public static final String SCOPE_EDIT_PROFILE = "edit-profile";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessLinkRepository accessLinkRepository;
    private final SupportWebClient supportWebClient;
    private final ProfileUpdatedEmailTemplateService profileUpdatedEmailTemplateService;
    private final DataProcessingConsentService dataProcessingConsentService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AccessLinkRepository accessLinkRepository,
                       SupportWebClient supportWebClient, ProfileUpdatedEmailTemplateService profileUpdatedEmailTemplateService, DataProcessingConsentService dataProcessingConsentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessLinkRepository = accessLinkRepository;
        this.supportWebClient = supportWebClient;
        this.profileUpdatedEmailTemplateService = profileUpdatedEmailTemplateService;
        this.dataProcessingConsentService = dataProcessingConsentService;
    }

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

    public User updateByMobileWithToken(String mobile, String token, User user, DataProcessingConsentCreationDto consentCreation, DeviceInfo deviceInfo) {
        User existingUser = this.readByMobile(mobile);
        if (!CUSTOMER.equals(existingUser.getRole())) {
            throw new ForbiddenException("Forbidden. Only CUSTOMER allowed. Role:" + existingUser.getRole() + "Mobile: " + mobile);
        }
        this.useAccessToken(mobile, token, true);
        boolean profileChanged = !EqualsBuilder.reflectionEquals(existingUser, user,
                "id", "password", "role", "registrationDate", "active");
        User userDB = this.updateUser(mobile, user);
        this.dataProcessingConsentService.create(userDB, token, consentCreation, deviceInfo);
        if (profileChanged) {
            try {
                this.supportWebClient.sendHtml(
                        this.profileUpdatedEmailTemplateService.buildHtmlEmail(
                                userDB.getEmail(),
                                userDB.getFirstName(),
                                userDB.getMobile(),
                                deviceInfo
                        )
                );
            } catch (Exception e) {
                throw new BadRequestException("Email incorrecto: (" + userDB.getEmail() + "). No se puede enviar notificaciones! ");
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
        if (Objects.isNull(user.getPassword())) {
            user.setPassword(existing.getPassword());
        } else {
            user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        }
        BeanUtils.copyProperties(user, existing, "id", "password", "role", "registrationDate", "active");
        return this.userRepository.save(existing);
    }

    private void validateAuthorizedRole(Role role) {
        List<Role> roles = this.validRoles();
        if (!roles.contains(role)) {
            throw new ForbiddenException("Insufficient role to update this user, role: " + role);
        }
    }

    private List<Role> validRoles() {
        Role authRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(Role::from)
                .orElse(Role.ANONYMOUS);
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
        this.useAccessToken(mobile, token, false);
        return this.readByMobile(mobile);
    }

    private void useAccessToken(String mobile, String token, boolean updating) {
        AccessLink accessLink = this.accessLinkRepository.findById(token)
                .orElseThrow(() -> new NotFoundException("The token don't exist: " + token));
        if (!accessLink.getUser().getMobile().equals(mobile)) {
            throw new ForbiddenException("Forbidden token. Token is the another mobile");
        }
        if (!accessLink.getScope().equals(SCOPE_EDIT_PROFILE)) {
            throw new ForbiddenException("Forbidden purpose. Only EDIT_PROFILE allowed.");
        }
        accessLink.use();
        if (updating) {
            accessLink.setLastUsedForUpdateAt(LocalDateTime.now());
        }
        this.accessLinkRepository.save(accessLink);
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

    public Stream<User> findNullSafe(UserFindCriteria criteria) {
        Stream<User> userDtos;
        if (criteria.all()) {
            userDtos = this.userRepository.findByRoleIn(validRoles()).stream();
        } else if (criteria.getAttribute() != null) {
            userDtos = this.userRepository.findByAll(criteria.getAttribute(), List.of(CUSTOMER)).stream();
        } else {
            userDtos = this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                    criteria.getMobile(), criteria.getFirstName(), criteria.getFamilyName(), criteria.getEmail(), criteria.getIdentity(), this.validRoles()
            ).stream();
        }

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(CUSTOMER.springSecurityAuthority())
                )
        ) {
            userDtos = userDtos.filter(user -> user.getMobile().equals(SecurityContextHolder.getContext().getAuthentication().getName()));
        }
        return userDtos;
    }

    public Stream<UUID> findIdsByMobileContaining(String mobile) {
        return this.userRepository.findByMobileContaining(mobile).stream()
                .map(User::getId);
    }

}
