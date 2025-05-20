package es.upm.api.resources;

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

    @Value("${info.app.artifact}")
    private String artifact;
    @Value("${info.app.version}")
    private String version;
    @Value("${info.app.build}")
    private String build;

    @GetMapping
    public String applicationInfo() {
        return """
                {"version":"%s::%s::%s"} (%s)
                """.formatted(this.artifact, this.version, this.build, LocalDateTime.now());
    }

    @GetMapping(value = VERSION_BADGE, produces = {"image/svg+xml"})
    public byte[] generateBadge() {
        return new Badge().generateBadge("AWS", "v" + version).getBytes();
    }

}
