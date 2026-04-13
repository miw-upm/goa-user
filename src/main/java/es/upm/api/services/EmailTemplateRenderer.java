package es.upm.api.services;

import es.upm.api.services.exceptions.InternalServerException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EmailTemplateRenderer {
    private EmailTemplateRenderer() {
    }

    public static String render(String templatePath, Map<String, String> variables) {
        String html = readClasspathFile(templatePath);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return html;
    }

    private static String readClasspathFile(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InternalServerException("Cannot load email template: " + path);
        }
    }
}
