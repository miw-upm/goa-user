package es.upm.api.services;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.api.data.entities.UserFindCriteria;
import es.upm.api.services.exceptions.ConflictException;
import es.upm.api.services.exceptions.ForbiddenException;
import es.upm.api.services.exceptions.NotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessLinkRepository accessLinkRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AccessLinkRepository accessLinkRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessLinkRepository = accessLinkRepository;
    }

    public void create(User user) {
        if (!authorizedScopes().contains(user.getRole())) {
            throw new ForbiddenException("Insufficient role to create this user: " + user);
        }
        this.assertNoExistByMobile(user.getMobile());
        this.assertNoExistByEmail(user.getEmail());
        this.assertNoExistByDni(user.getIdentity());
        user.setId(UUID.randomUUID());
        if (Objects.isNull(user.getPassword())) {
            user.setPassword(this.passwordEncoder.encode(UUID.randomUUID().toString()));
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(LocalDate.now());
        this.userRepository.save(user);
    }

    public User updateByMobile(String mobile, User user) {
        if (!authorizedScopes().contains(user.getRole())) {
            throw new ForbiddenException("Insufficient role to update this user: " + user);
        }
        User userBD = this.readByMobile(mobile);
        if (!mobile.equals(user.getMobile())) {
            this.assertNoExistByMobile(user.getMobile());
        }
        if (!Objects.equals(userBD.getEmail(), user.getEmail())) {
            this.assertNoExistByEmail(user.getEmail());
        }
        if (!Objects.equals(userBD.getIdentity(), user.getIdentity())) {
            this.assertNoExistByDni(user.getIdentity());
        }
        if (Objects.isNull(user.getPassword())) {
            user.setPassword(userBD.getPassword());
        } else {
            user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        }
        BeanUtils.copyProperties(user, userBD, "id", "registrationDate");
        return this.userRepository.save(userBD);
    }

    public User updateByMobileWithToken(String mobile, String token, User user) {
        this.useAccessToken(mobile, token);
        user.setRole(Role.CUSTOMER);
        return this.updateByMobile(mobile, user);
    }

    private List<Role> authorizedScopes() {
        Role role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(Role::of)
                .orElse(Role.ANONYMOUS);

        return switch (role) {
            case ADMIN -> List.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR, Role.CUSTOMER);
            case MANAGER -> List.of(Role.MANAGER, Role.OPERATOR, Role.CUSTOMER);
            case OPERATOR, CUSTOMER, URL_TOKEN -> List.of(Role.CUSTOMER);
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
        if (!this.useAccessToken(mobile, token).equals("EDIT_PROFILE")) {
            throw new ForbiddenException("Forbidden purpose");
        }
        return this.readByMobile(mobile);
    }

    private String useAccessToken(String mobile, String token) {
        AccessLink accessLink = this.accessLinkRepository.findById(token)
                .orElseThrow(() -> new NotFoundException("The token don't exist: " + token));
        if (!accessLink.getUser().getMobile().equals(mobile)) {
            throw new ForbiddenException("Forbidden token");
        }
        accessLink.use();
        this.accessLinkRepository.save(accessLink);
        return accessLink.getPurpose();
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
            userDtos = this.userRepository.findByRoleIn(authorizedScopes()).stream();
        } else {
            userDtos = this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                    criteria.getMobile(), criteria.getFirstName(), criteria.getFamilyName(), criteria.getEmail(), criteria.getIdentity(), this.authorizedScopes()
            ).stream();
        }
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(Role.CUSTOMER.roleValue())
                )
        ) {
            userDtos = userDtos.filter(user -> user.getMobile().equals(SecurityContextHolder.getContext().getAuthentication().getName()));
        }
        return userDtos;

    }

}
