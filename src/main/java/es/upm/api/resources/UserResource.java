package es.upm.api.resources;

import es.upm.api.data.entities.Province;
import es.upm.api.resources.dtos.ProvincesDto;
import es.upm.api.resources.dtos.UserDto;
import es.upm.api.resources.dtos.validations.Validations;
import es.upm.api.services.UserFindCriteria;
import es.upm.api.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Log4j2
@PreAuthorize(Security.AUTHENTICATED)
@RestController
@RequestMapping(UserResource.USERS)
public class UserResource {
    public static final String USERS = "/users";
    public static final String ID_ID = "/{id}";
    public static final String MOBILE_ID_TOKEN_ID = "/{mobile}/{token}";
    public static final String PROVINCES = "/provinces";
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
    @GetMapping(Validations.ID_WITH_UUID)
    public UserDto readById(@PathVariable UUID id) {
        return new UserDto(this.userService.read(id));
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_URL_TOKEN)
    @GetMapping(Validations.ID_WITH_MOBILE)
    public UserDto readByMobile(@PathVariable("id") String mobile) {
        return new UserDto(this.userService.readByMobile(mobile));
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(MOBILE_ID_TOKEN_ID)
    public UserDto readByMobileWithToken(@PathVariable String mobile, @PathVariable String token) {
        return new UserDto(this.userService.readByMobileWithToken(mobile, token))
                .ofAllBasic();
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
    @PutMapping(Validations.ID_WITH_MOBILE)
    public UserDto updateByMobile(@PathVariable("id") String mobile, @Valid @RequestBody UserDto userDto) {
        return new UserDto(this.userService.updateByMobile(mobile, userDto.toUser()));
    }

    @PreAuthorize(Security.ALL)
    @PutMapping(MOBILE_ID_TOKEN_ID)
    public UserDto updateByMobileWithToken(@PathVariable String mobile, @PathVariable String token, @Valid @RequestBody UserDto userDto) {
        return new UserDto(this.userService.updateByMobileWithToken(mobile, token, userDto.toUser()))
                .ofAllBasic();
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_CUSTOMER)
    @GetMapping
    public List<UserDto> findNullSafe(@ModelAttribute UserFindCriteria criteria) {
        Stream<UserDto> userDtos = this.userService.findNullSafe(criteria)
                .map(UserDto::new);
        if (criteria.isProjection()) {
            return userDtos.toList();
        } else {
            return userDtos.map(UserDto::ofMobileFirstNameFamilyNameEmail).toList();
        }
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(PROVINCES)
    public ProvincesDto findProvinces() {
        return new ProvincesDto(Arrays.stream(Province.values())
                .map(Province::name)
                .toList());
    }

}
