package es.upm.api.services;

import es.upm.miw.device.DeviceInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ProfileUpdatedEmailTemplateService {
    private static final String PROFILE_UPDATED_SUBJECT = "Actualización de perfil en Ocaña Abogados";
    private static final String HTML_TEMPLATE_PATH = "templates/email/profile-updated.html";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Email buildHtmlEmail(String to, String firstName, String mobile,
                                String token, DeviceInfo deviceInfo) {
        String body = EmailTemplateRenderer.render(HTML_TEMPLATE_PATH, Map.of(
                "FIRST_NAME", firstName,
                "UPDATED_AT", LocalDateTime.now().format(DATE_TIME_FORMATTER),
                "MOBILE", mobile,
                "TOKEN", token,
                "CLIENT_IP", deviceInfo.getIpAddress(),
                "DEVICE_TYPE", deviceInfo.getDeviceType(),
                "OPERATING_SYSTEM", deviceInfo.getOperatingSystem(),
                "BROWSER", deviceInfo.getBrowser()
        ));
        return Email.builder()
                .to(to)
                .subject(PROFILE_UPDATED_SUBJECT)
                .body(body)
                .build();
    }
}
