package es.upm.api.configurations;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
@Order(2)
public class AdminUserInitializer implements ApplicationRunner {

    private static final UUID ADMIN_ID = UUID.nameUUIDFromBytes("admin-seed".getBytes());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.db.admin}")
    private String admin;
    @Value("${app.db.mobile}")
    private String mobile;
    @Value("${app.db.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (!this.userRepository.findByRoleIn(List.of(Role.ADMIN)).isEmpty()) {
            return;
        }
        this.userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .mobile(this.mobile)
                .firstName(this.admin)
                .password(this.passwordEncoder.encode(this.password))
                .role(Role.ADMIN)
                .registrationDate(LocalDate.now())
                .active(true)
                .build()
        );
        log.warn("Admin user initialized");
    }
}
