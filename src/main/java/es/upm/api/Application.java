package es.upm.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@EnableFeignClients
public class Application {

    public static void main(String[] args) { // mvn clean spring-boot:run
        SpringApplication.run(Application.class, args);
    }

}
