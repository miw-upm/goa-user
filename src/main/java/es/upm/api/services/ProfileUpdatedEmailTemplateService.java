package es.upm.api.services;

import es.upm.api.services.exceptions.InternalServerException;
import es.upm.miw.device.DeviceInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ProfileUpdatedEmailTemplateService {
    private static final String PROFILE_UPDATED_SUBJECT = "Actualización de perfil en Ocaña Abogados";
    private static final String HTML_TEMPLATE_PATH = "templates/email/profile-updated.html";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String htmlTemplate;

    public ProfileUpdatedEmailTemplateService() {
        this.htmlTemplate = readClasspathFile(HTML_TEMPLATE_PATH);
    }

    public Email buildHtmlEmail(String to, String firstName, String mobile, String token, DeviceInfo deviceInfo) {
        String renderedHtml = this.htmlTemplate
                .replace("{{FIRST_NAME}}", firstName)
                .replace("{{UPDATED_AT}}", LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .replace("{{MOBILE}}", mobile)
                .replace("{{TOKEN}}", token)
                .replace("{{CLIENT_IP}}", deviceInfo.getIpAddress())
                .replace("{{DEVICE_TYPE}}", deviceInfo.getDeviceType())
                .replace("{{OPERATING_SYSTEM}}", deviceInfo.getOperatingSystem())
                .replace("{{BROWSER}}", deviceInfo.getBrowser());

        return Email.builder()
                .to(to)
                .subject(PROFILE_UPDATED_SUBJECT)
                .body(renderedHtml)
                .build();
    }

    private String readClasspathFile(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InternalServerException("Cannot load email template: " + path);
        }
    }
}
