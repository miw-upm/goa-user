package es.upm.api.services.feign;

import es.upm.api.configurations.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = SupportWebClient.GOA_SUPPORT, configuration = FeignConfig.class)
public interface SupportWebClient {
    String GOA_SUPPORT = "goa-support";
    String EMAILS = "/emails";
    String SIMPLE = "/simple";
    String HTML = "/html";

    @PostMapping(EMAILS + SIMPLE)
    void sendSimple(@RequestBody EmailDto emailDto);

    @PostMapping(EMAILS + HTML)
    void sendHtml(@RequestBody EmailDto emailDto);

}
