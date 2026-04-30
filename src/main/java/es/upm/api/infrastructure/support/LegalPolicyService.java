package es.upm.api.infrastructure.support;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LegalPolicyService {
    private static final String LOCATION_PATTERN = "classpath:legal/lopd.*.html";
    private static final Pattern FILE_PATTERN =
            Pattern.compile("lopd\\.(\\d{4}-\\d{2}-\\d{2})\\.html");
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public String currentPolicyVersion() {
        try {
            return Arrays.stream(resolver.getResources(LOCATION_PATTERN))
                    .map(Resource::getFilename)
                    .filter(Objects::nonNull)
                    .map(FILE_PATTERN::matcher)
                    .filter(Matcher::matches)
                    .map(m -> m.group(1))
                    .max(Comparator.naturalOrder())
                    .orElseThrow(() -> new IllegalStateException(
                            "No se encontró ningún fichero LOPD en resources/legal/"));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron leer las políticas LOPD", e);
        }
    }
}
