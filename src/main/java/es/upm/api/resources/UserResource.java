package es.upm.api.resources;

import es.upm.api.data.entities.Province;
import es.upm.api.data.entities.User;
import es.upm.api.resources.dtos.DataProcessingConsentCreationDto;
import es.upm.api.resources.dtos.ProvincesDto;
import es.upm.api.resources.dtos.UserDto;
import es.upm.api.resources.dtos.UserUpdateWithConsentDto;
import es.upm.api.resources.dtos.validations.Validations;
import es.upm.api.services.UserService;
import es.upm.api.services.criteria.UserFindCriteria;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.device.DeviceInfoResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@PreAuthorize(Security.AUTHENTICATED)
@RestController
@RequestMapping(UserResource.USERS)
public class UserResource {
    public static final String USERS = "/users";
    public static final String MOBILE_ID_TOKEN_ID = "/{mobile}/{token}";
    public static final String PROVINCES = "/provinces";

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
                .ofBasic();
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
    @PutMapping(Validations.ID_WITH_MOBILE)
    public UserDto updateByMobile(@PathVariable("id") String mobile, @Valid @RequestBody UserDto userDto) {
        return new UserDto(this.userService.updateByMobile(mobile, userDto.toDomain()));
    }

    @PreAuthorize(Security.ALL)
    @PutMapping(MOBILE_ID_TOKEN_ID)
    public UserDto updateByMobileWithToken(@PathVariable String mobile, @PathVariable String token,
                                           @Valid @RequestBody UserUpdateWithConsentDto body,
                                           HttpServletRequest request) {
        User user = body.getUserDto().toDomain();
        DataProcessingConsentCreationDto consent = body.getDataProcessingConsentCreationDto();
        return new UserDto(this.userService.updateByMobileWithToken(mobile, token, user,
                consent.getDataProcessingAccepted(), consent.getPromotionsAccepted(),
                resolveDeviceInfo(request))).ofBasic();
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_CUSTOMER)
    @GetMapping
    public List<UserDto> find(@ModelAttribute UserFindCriteria criteria) {
        return this.userService.find(criteria)
                .map(user -> UserDto.of(user, criteria.isProjection()))
                .toList();
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(PROVINCES)
    public ProvincesDto findProvinces() {
        return new ProvincesDto(Arrays.stream(Province.values())
                .map(Province::name)
                .toList());
    }

    private DeviceInfo resolveDeviceInfo(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIp = request.getHeader("X-Real-IP");
        String ip;
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            ip = xForwardedFor.split(",")[0].trim();
        } else if (xRealIp != null && !xRealIp.isBlank()) {
            ip = xRealIp.trim();
        } else {
            ip = request.getRemoteAddr();
        }
        return DeviceInfoResolver.resolve(request.getHeader("User-Agent"), ip);
    }

}
