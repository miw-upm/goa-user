package es.upm.api.resources;

import es.upm.api.data.entities.Province;
import es.upm.api.data.entities.User;
import es.upm.api.resources.dtos.DataProcessingConsentCreationDto;
import es.upm.api.resources.dtos.ProvincesResponseDto;
import es.upm.api.resources.dtos.UserAndConsentUpdatingDto;
import es.upm.api.resources.dtos.UserDto;
import es.upm.api.services.UserService;
import es.upm.api.services.criteria.UserFindCriteria;
import es.upm.miw.device.DeviceInfoResolver;
import es.upm.miw.security.Security;
import es.upm.miw.security.Validations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@PreAuthorize(Security.AUTHENTICATED)
@RestController
@RequestMapping(UserResource.USERS)
@RequiredArgsConstructor
@Log4j2
public class UserResource {
    public static final String USERS = "/users";
    public static final String SCOPE_ID_ID_ID_TOKEN_ID = "/{scope}/{id}/{token}";
    public static final String PROVINCES = "/provinces";
    public static final String FULL = "/full";

    private final UserService userService;

    @PreAuthorize(Security.ALL)
    @PostMapping
    public void create(@Valid @RequestBody UserDto userDto) {
        userDto.doDefault();
        this.userService.create(userDto.toDomain());
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_URL_TOKEN)
    @GetMapping(Validations.ID_WITH_UUID)
    public UserDto readById(@PathVariable UUID id) {
        return new UserDto(this.userService.readById(id));
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_URL_TOKEN)
    @GetMapping(Validations.ID_WITH_MOBILE)
    public UserDto readByMobile(@PathVariable("id") String mobile) {
        return new UserDto(this.userService.readByMobile(mobile));
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
    @PutMapping(Validations.ID_WITH_UUID)
    public UserDto update(@PathVariable("id") UUID id, @Valid @RequestBody UserDto userDto) {
        return new UserDto(this.userService.update(id, userDto.toDomain()));
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
    @GetMapping
    public List<UserDto> find(@ModelAttribute UserFindCriteria criteria) {
        return this.userService.find(criteria)
                .map(UserDto::new)
                .map(UserDto::ofSummary)
                .toList();
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(PROVINCES)
    public ProvincesResponseDto findProvinces() {
        return new ProvincesResponseDto(Arrays.stream(Province.values())
                .map(Province::name)
                .toList());
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(SCOPE_ID_ID_ID_TOKEN_ID)
    public UserDto readByUrlIdWithToken(@PathVariable String scope, @PathVariable String id, @PathVariable String token) {
        return new UserDto(this.userService.readByMobileWithToken(scope, id, token))
                .ofProfile();
    }

    @PreAuthorize(Security.ALL)
    @PutMapping(SCOPE_ID_ID_ID_TOKEN_ID)
    public UserDto updateByUrlIdWithToken(@PathVariable String scope, @PathVariable String id, @PathVariable String token,
                                          @Valid @RequestBody UserAndConsentUpdatingDto body,
                                          HttpServletRequest request) {
        User user = body.getUser().toDomain();
        DataProcessingConsentCreationDto consent = body.getDataProcessingConsentCreation();
        return new UserDto(this.userService.updateByUrlIdWithToken(scope, id, token, user,
                consent.getDataProcessingAccepted(), consent.getPromotionsAccepted(),
                DeviceInfoResolver.resolve(request))).ofProfile();
    }

    @PreAuthorize(Security.ADMIN)
    @GetMapping(value = FULL)
    public List<UserDto> findAllFull() {
        return this.userService.findAllFull()
                .map(UserDto::new)
                .toList();
    }

}
