package es.upm.api.resources;

import es.upm.api.resources.dtos.ApplicationInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(SystemResource.SYSTEM)
public class SystemResource {
    public static final String SYSTEM = "/system";
    public static final String VERSION_BADGE = "/version-badge";
    private static final String BADGE_IMAGE_TEMPLATE = """
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="20">
                <linearGradient id="a" x2="0" y2="100%%">
                    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                </linearGradient>
                <rect rx="3" width="%d" height="20" fill="#555"/>
                <rect rx="3" x="%d" width="%d" height="20" fill="#4c1"/>
                <path fill="#4c1" d="M%d 0h4v20h-4z"/>
                <rect rx="3" width="%d" height="20" fill="url(#a)"/>
                <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
                    <text x="%d" y="15" fill="#010101" fill-opacity=".3">%s</text>
                    <text x="%d" y="14">%s</text>
                    <text x="%d" y="15" fill="#010101" fill-opacity=".3">%s</text>
                    <text x="%d" y="14">%s</text>
                </g>
            </svg>
            """;
    private static final int TEXT_MARGIN = 12;

    @Value("${info.app.artifact}")
    private String artifact;
    @Value("${info.app.version}")
    private String version;
    @Value("${info.app.build}")
    private String build;
    @Value("${app.hosting}")
    private String hosting;

    @GetMapping
    public ApplicationInfoDto applicationInfo() {
        return new ApplicationInfoDto(
                "%s::%s::%s".formatted(artifact, version, build),
                LocalDateTime.now()
        );
    }

    @GetMapping(value = VERSION_BADGE, produces = {"image/svg+xml"})
    public byte[] generateBadge() {
        return this.generateBadge(hosting + ": " + artifact, "v" + version).getBytes();
    }

    private int measureText(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += switch (c) {
                case 'i', 'l', 'I', 'j', '.', ',', ';', ':', '\'', '|' -> 3;
                case 'f', 'r', 't', '(', ')', '[', ']', '{', '}', ' ' -> 4;
                case 'm', 'M', 'w', 'W' -> 9;
                default -> 6;
            };
        }
        return width;
    }

    private String generateBadge(String label, String value) {
        int widthLabel = TEXT_MARGIN + measureText(label);
        int widthValue = TEXT_MARGIN + measureText(value);
        int textWidth = widthLabel + widthValue;
        int middleLabel = widthLabel / 2;
        int middleValue = widthLabel + widthValue / 2;
        return String.format(BADGE_IMAGE_TEMPLATE, textWidth, textWidth, widthLabel, widthValue,
                widthLabel, textWidth,
                middleLabel, label, middleLabel, label, middleValue, value, middleValue, value);
    }

}
