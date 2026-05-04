package es.upm.api.infrastructure.migrations;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.User;
import es.upm.api.infrastructure.support.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Log4j2
@Profile("prod")
public class UserMigration implements ApplicationRunner {
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int migratedUsers = 0;
        for (User user : userRepository.findAll()) {
            if (migrateUser(user)) {
                userRepository.save(user);
                migratedUsers++;
            }
        }
        log.warn("User sensitive fields migration finished. Migrated users: {}", migratedUsers);
    }

    private boolean migrateUser(User user) {
        boolean changed = false;
        String identity = this.migrateEncryptedField(user.getIdentity());
        if (!Objects.equals(user.getIdentity(), identity)) {
            user.setIdentity(identity);
            changed = true;
        }
        String email = this.migrateEncryptedField(user.getEmail());
        if (!Objects.equals(user.getEmail(), email)) {
            user.setEmail(email);
            changed = true;
        }
        String address = this.migrateEncryptedField(user.getAddress());
        if (!Objects.equals(user.getAddress(), address)) {
            user.setAddress(address);
            changed = true;
        }
        return changed;
    }

    private String migrateEncryptedField(String value) {
        if (encryptionService.isEncrypted(value)) {
            return value;
        }
        return encryptionService.encrypt(value);
    }

}
