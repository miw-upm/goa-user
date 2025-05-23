package es.upm.api.resources;

import es.upm.api.data.entities.UserFindCriteria;
import es.upm.api.resources.view.UserDto;
import es.upm.api.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Stream;

@Log4j2
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(UserResource.USERS)
public class UserResource {
    public static final String USERS = "/users";
    public static final String ID_ID = "/{id}";
    public static final String MOBILE = "/mobile";
    public static final String MOBILE_ID = "/{mobile}";
    public static final String TOKEN_ID = "/{token}";
    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize(Security.ALL)
    @PostMapping
    public void create(@Valid @RequestBody UserDto creationUserDto) {
        creationUserDto.doDefault();
        this.userService.create(creationUserDto.toUser());
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_URL_TOKEN)
    @GetMapping(ID_ID)
    public UserDto read(@PathVariable UUID id) {
        return new UserDto(this.userService.read(id));
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_URL_TOKEN)
    @GetMapping(MOBILE + MOBILE_ID)
    public UserDto readByMobile(@PathVariable String mobile) {
        return new UserDto(this.userService.readByMobile(mobile));
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(MOBILE + MOBILE_ID + TOKEN_ID)
    public UserDto readByMobileWithToken(@PathVariable String mobile, @PathVariable String token) {
        return new UserDto(this.userService.readByMobileWithToken(mobile, token))
                .ofMobileFirstNameFamilyNameEmailDocumentTypeIdentityAddress();
    }

    @PutMapping(MOBILE + MOBILE_ID)
    public UserDto updateByMobile(@PathVariable String mobile, @Valid @RequestBody UserDto userDto) {
        return new UserDto(this.userService.updateByMobile(mobile, userDto.toUser()));
    }

    @PreAuthorize(Security.ALL)
    @PutMapping(MOBILE + MOBILE_ID + TOKEN_ID)
    public UserDto updateByMobileWithToken(@PathVariable String mobile, @PathVariable String token, @Valid @RequestBody UserDto userDto) {
        return new UserDto(this.userService.updateByMobileWithToken(mobile, token, userDto.toUser()))
                .ofMobileFirstNameFamilyNameEmailDocumentTypeIdentityAddress();
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_CUSTOMER)
    @GetMapping
    public Stream<UserDto> findNullSafe(@ModelAttribute UserFindCriteria criteria) {
        Stream<UserDto> userDtos = this.userService.findNullSafe(criteria)
                .map(UserDto::new);
        if (criteria.isProjection()) {
            return userDtos;
        } else {
            return userDtos.map(UserDto::ofMobileFirstNameFamilyNameEmail);
        }
    }

}
