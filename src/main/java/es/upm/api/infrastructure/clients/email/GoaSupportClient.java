package es.upm.api.infrastructure.clients.email;

import es.upm.api.configurations.FeignConfig;
import es.upm.miw.mail.Email;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = GoaSupportClient.GOA_SUPPORT, configuration = FeignConfig.class)
public interface GoaSupportClient {
    String GOA_SUPPORT = "goa-support";
    String EMAILS = "/emails";
    String HTML = "/html";

    @PostMapping(EMAILS + HTML)
    void sendHtml(@RequestBody Email email);

}
