package es.upm.api.services.emailoutport;

import es.upm.api.configurations.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = EmailPort.GOA_SUPPORT, configuration = FeignConfig.class)
public interface EmailPort {
    String GOA_SUPPORT = "goa-support";
    String EMAILS = "/emails";
    String SIMPLE = "/simple";
    String HTML = "/html";

    @PostMapping(EMAILS + SIMPLE)
    void sendSimple(@RequestBody EmailRequest email);

    @PostMapping(EMAILS + HTML)
    void sendHtml(@RequestBody EmailRequest email);

}
