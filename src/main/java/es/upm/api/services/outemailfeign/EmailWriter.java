package es.upm.api.services.outemailfeign;

import es.upm.api.configurations.FeignConfig;
import es.upm.miw.mail.Email;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = EmailWriter.GOA_SUPPORT, configuration = FeignConfig.class)
public interface EmailWriter {
    String GOA_SUPPORT = "goa-support";
    String EMAILS = "/emails";
    String SIMPLE = "/simple";
    String HTML = "/html";

    @PostMapping(EMAILS + SIMPLE)
    void sendSimple(@RequestBody Email email);

    @PostMapping(EMAILS + HTML)
    void sendHtml(@RequestBody Email email);

}
