package es.upm.api.infrastructure.support;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.User;
import es.upm.miw.exception.ConflictException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.stream.Stream;

@Service
@Log4j2
@Profile("NONE")
public class UserFieldMigrationOnStartup implements ApplicationRunner {
    private static final String LEGACY_PREFIX = "enc::";
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final TextEncryptor legacyTextEncryptor;

    public UserFieldMigrationOnStartup(
            UserRepository userRepository,
            EncryptionService encryptionService,
            @Qualifier("legacyTextEncryptor") @Nullable TextEncryptor legacyTextEncryptor
    ) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        if (legacyTextEncryptor == null) {
            throw new ConflictException("No legacy key configured for rotation");
        }
        this.legacyTextEncryptor = legacyTextEncryptor;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.rotateLegacyToCurrentPrefix();
    }

    public void rotateLegacyToCurrentPrefix() {
        log.warn("Starting legacy user field migration - this should only run during a controlled migration window");
        int usersProcessed = 0;
        int usersUpdated = 0;
        int fieldsRotated = 0;
        try (Stream<User> stream = this.userRepository.findAll().stream()) {
            Iterator<User> it = stream.iterator();
            while (it.hasNext()) {
                User user = it.next();
                usersProcessed++;
                int updated = this.rotateUser(user);
                if (updated > 0) {
                    usersUpdated++;
                    fieldsRotated += updated;
                }
            }
        }
        log.warn("Legacy user field migration completed - users processed: {}, users updated: {}, fields rotated: {}",
                usersProcessed, usersUpdated, fieldsRotated);
    }

    private int rotateUser(User user) {
        int updated = 0;
        String newEmail = this.tryRotateField(user.getEmail());
        if (newEmail != null) {
            user.setEmail(newEmail);
            updated++;
        }
        String newIdentity = this.tryRotateField(user.getIdentity());
        if (newIdentity != null) {
            user.setIdentity(newIdentity);
            updated++;
        }
        String newAddress = this.tryRotateField(user.getAddress());
        if (newAddress != null) {
            user.setAddress(newAddress);
            updated++;
        }
        if (updated > 0) {
            log.warn("Re-encrypted legacy fields - user id: {}, fields rotated: {}", user.getId(), updated);
            this.userRepository.save(user);
        }
        return updated;
    }

    private String tryRotateField(String value) {
        if (value == null || !value.startsWith(LEGACY_PREFIX)) {
            return null;
        }
        String legacyPayload = value.substring(LEGACY_PREFIX.length());
        return this.encryptionService.encrypt(this.legacyTextEncryptor.decrypt(legacyPayload));
    }
}
