package es.upm.api.configurations;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.daos.DataProcessingConsentRepository;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.*;
import es.upm.api.services.UserService;
import es.upm.api.services.infrastructure.EncryptionService;
import es.upm.miw.device.DeviceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Component
@Profile({"dev", "test"})
@Order(1)
@RequiredArgsConstructor
public class SeederForDev implements ApplicationRunner {
    public static final String PREFIX = "aaaaaaaa-bbbb-cccc-dddd-eeeeffff";
    public static final UUID ID_0 = UUID.fromString(PREFIX + "0000");
    public static final UUID ID_1 = UUID.fromString(PREFIX + "0001");
    public static final UUID ID_2 = UUID.fromString(PREFIX + "0002");
    public static final UUID ID_3 = UUID.fromString(PREFIX + "0003");
    public static final UUID ID_4 = UUID.fromString(PREFIX + "0004");
    public static final UUID ID_5 = UUID.fromString(PREFIX + "0005");
    public static final UUID ID_6 = UUID.fromString(PREFIX + "0006");
    public static final UUID ID_7 = UUID.fromString(PREFIX + "0007");
    public static final UUID ID_8 = UUID.fromString(PREFIX + "0008");
    public static final UUID ID_9 = UUID.fromString(PREFIX + "0009");
    public static final UUID ID_A = UUID.fromString(PREFIX + "000a");
    public static final UUID ID_B = UUID.fromString(PREFIX + "000b");
    public static final UUID ID_C = UUID.fromString(PREFIX + "000c");
    public static final UUID ID_D = UUID.fromString(PREFIX + "000d");
    public static final UUID ID_E = UUID.fromString(PREFIX + "000e");
    public static final UUID ID_F = UUID.fromString(PREFIX + "000f");
    public static final String PASSWORD = "6";

    public static final User C_0 = User.builder()
            .id(ID_0)
            .mobile("600000100")
            .firstName("cliente0")
            .familyName("García López")
            .password(PASSWORD)
            .role(Role.CUSTOMER)
            .identity("00000000T")
            .address("C/ Alcalá, 100")
            .email("cliente0@example.com")
            .city("Madrid")
            .province(Province.MADRID)
            .postalCode(28001)
            .registrationDate(LocalDate.of(2024, 1, 1))
            .active(true)
            .build();

    public static final User C_1 = User.builder()
            .id(ID_1)
            .mobile("600000101")
            .firstName("cliente1")
            .familyName("Martínez Ruiz")
            .password(PASSWORD)
            .role(Role.CUSTOMER)
            .identity("00000001R")
            .address("C/ Sierpes, 101")
            .email("cliente1@example.com")
            .city("Sevilla")
            .province(Province.SEVILLA)
            .postalCode(41001)
            .registrationDate(LocalDate.of(2025, 2, 1))
            .active(true)
            .build();

    public static final User C_2 = User.builder()
            .id(ID_2)
            .mobile("600000102")
            .firstName("cliente2")
            .familyName("Sánchez Pérez")
            .role(Role.CUSTOMER)
            .identity("00000002W")
            .address("Av. Andalucía, 102")
            .email("cliente2@example.com")
            .city("Cádiz")
            .province(Province.CADIZ)
            .postalCode(11001)
            .registrationDate(LocalDate.of(2025, 3, 1))
            .active(true)
            .build();

    public static final User C_3 = User.builder()
            .id(ID_3)
            .mobile("600000103")
            .firstName("cliente3")
            .familyName("Fernández Torres")
            .role(Role.CUSTOMER)
            .identity("00000003A")
            .address("C/ Mayor, 103")
            .email("cliente3@example.com")
            .city("Madrid")
            .province(Province.MADRID)
            .postalCode(28013)
            .registrationDate(LocalDate.of(2025, 4, 1))
            .active(true)
            .build();

    public static final User C_4 = User.builder()
            .id(ID_4)
            .mobile("600000104")
            .firstName("cliente4")
            .familyName("Romero Navarro")
            .role(Role.CUSTOMER)
            .identity("00000004G")
            .address("Av. Constitución, 104")
            .email("cliente4@example.com")
            .city("Sevilla")
            .province(Province.SEVILLA)
            .postalCode(41004)
            .registrationDate(LocalDate.of(2025, 5, 1))
            .active(true)
            .build();

    public static final User C_5 = User.builder()
            .id(ID_5)
            .mobile("600000105")
            .firstName("cliente5")
            .familyName("Moreno Castro")
            .role(Role.CUSTOMER)
            .identity("00000005M")
            .address("C/ Ancha, 105")
            .email("cliente5@example.com")
            .city("Cádiz")
            .province(Province.CADIZ)
            .postalCode(11005)
            .registrationDate(LocalDate.of(2025, 6, 1))
            .active(true)
            .build();

    public static final User C_6 = User.builder()
            .id(ID_6)
            .mobile("600000106")
            .firstName("Cliente6")
            .role(Role.CUSTOMER)
            .registrationDate(LocalDate.of(2025, 6, 7))
            .active(true)
            .build();

    public static final User C_7 = User.builder()
            .id(ID_7)
            .mobile("600000107")
            .firstName("Cliente7")
            .role(Role.CUSTOMER)
            .registrationDate(LocalDate.of(2025, 7, 7))
            .active(true)
            .build();

    public static final User C_8 = User.builder()
            .id(ID_8)
            .mobile("600000108")
            .firstName("Cliente8")
            .role(Role.CUSTOMER)
            .registrationDate(LocalDate.of(2025, 8, 7))
            .active(true)
            .build();

    public static final User C_9 = User.builder()
            .id(ID_9)
            .mobile("600000109")
            .firstName("Cliente9")
            .role(Role.CUSTOMER)
            .registrationDate(LocalDate.of(2025, 9, 7))
            .active(true)
            .build();

    public static final User ADMIN = User.builder()
            .id(ID_D)
            .mobile("600000110")
            .firstName("Admin1")
            .familyName("García López")
            .role(Role.ADMIN)
            .identity("00000010X")
            .address("C/ Gran Vía, 10")
            .email("admin1@example.com")
            .city("Madrid")
            .province(Province.MADRID)
            .postalCode(28013)
            .registrationDate(LocalDate.of(2025, 10, 1))
            .active(true)
            .build();

    public static final User MANAGER = User.builder()
            .id(ID_E)
            .mobile("600000111")
            .firstName("Manager1")
            .familyName("Martínez Ruiz")
            .role(Role.MANAGER)
            .identity("00000011B")
            .address("C/ Sierpes, 11")
            .email("manager1@example.com")
            .city("Sevilla")
            .province(Province.SEVILLA)
            .postalCode(41001)
            .registrationDate(LocalDate.of(2025, 11, 1))
            .active(true)
            .build();

    public static final User OPERATOR = User.builder()
            .id(ID_F)
            .mobile("600000112")
            .firstName("Operator1")
            .familyName("Sánchez Pérez")
            .role(Role.OPERATOR)
            .identity("00000012N")
            .address("C/ Ancha, 12")
            .email("operator1@example.com")
            .city("Cádiz")
            .province(Province.CADIZ)
            .postalCode(11005)
            .registrationDate(LocalDate.of(2025, 12, 1))
            .active(true)
            .build();

    public static final String TOKEN_0 = "fffffffffffffffffffffffffffffffffffffffff0";
    public static final String TOKEN_1 = "fffffffffffffffffffffffffffffffffffffffff1";
    public static final String TOKEN_2 = "fffffffffffffffffffffffffffffffffffffffff2";
    public static final String TOKEN_3 = "fffffffffffffffffffffffffffffffffffffffff3";

    public static final String URL_0 = "aaaaaaaaaaaaaaaaaaaaa0";
    public static final String URL_1 = "aaaaaaaaaaaaaaaaaaaaa1";
    public static final String URL_2 = "aaaaaaaaaaaaaaaaaaaaa2";
    public static final String URL_3 = "aaaaaaaaaaaaaaaaaaaaa3";

    private final UserRepository userRepository;
    private final UserService userService;
    private final AccessLinkRepository accessLinkRepository;
    private final DataProcessingConsentRepository dataProcessingConsentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService  encryptionService;

    @Value("${app.db.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        this.deleteAll();
        this.seed();
    }

    private void deleteAll() {
        this.dataProcessingConsentRepository.deleteAll();
        this.accessLinkRepository.deleteAll();
        this.userRepository.deleteAll();
        log.warn("------- Deleted All -----------");
    }

    private void seed() {
        log.warn("------- Initial Load from JAVA -----------");
        String pass = this.passwordEncoder.encode(this.password);
        DeviceInfo deviceInfo = DeviceInfo.builder().ipAddress("83.52.10.24").browser("Chrome")
                .operatingSystem("Windows").deviceType("Desktop").build();

        List<User> users = List.of(
                withEncodedPassword(C_0, pass),
                withEncodedPassword(C_1, pass),
                withEncodedPassword(C_2, pass),
                withEncodedPassword(C_3, pass),
                withEncodedPassword(C_4, pass),
                withEncodedPassword(C_5, pass),
                withEncodedPassword(C_6, pass),
                withEncodedPassword(C_7, pass),
                withEncodedPassword(C_8, pass),
                withEncodedPassword(C_9, pass),
                withEncodedPassword(ADMIN, pass),
                withEncodedPassword(MANAGER, pass),
                withEncodedPassword(OPERATOR, pass)
        );
        users.forEach(user -> user.setAddress(this.encryptionService.encrypt(user.getAddress())));
        this.userRepository.saveAll(users);
        log.warn("        ------- users");

        LocalDateTime now = LocalDateTime.now();
        List<AccessLink> accessLinks = List.of(
                AccessLink.builder()
                        .id(ID_0)
                        .urlId(URL_0)
                        .user(C_0)
                        .tokenHash(passwordEncoder.encode(TOKEN_0))
                        .createdAt(now.minusDays(1))
                        .lastUsedAt(now)
                        .expiresAt(now.plusDays(5))
                        .remainingUses(4)
                        .scope("edit-profile")
                        .build(),

                AccessLink.builder()
                        .id(ID_1)
                        .urlId(URL_1)
                        .user(C_1)
                        .tokenHash(passwordEncoder.encode(TOKEN_1))
                        .createdAt(now.minusDays(2))
                        .lastUsedAt(now.minusDays(1))
                        .expiresAt(now.plusDays(5))
                        .remainingUses(3)
                        .scope("edit-profile")
                        .build(),

                AccessLink.builder()
                        .id(ID_2)
                        .urlId(URL_2)
                        .user(C_2)
                        .tokenHash(passwordEncoder.encode(TOKEN_2))
                        .createdAt(now.minusDays(1))
                        .expiresAt(now.plusDays(5))
                        .remainingUses(2)
                        .scope("read-engagement-letter")
                        .documentId(ID_2)
                        .build(),

                AccessLink.builder()
                        .id(ID_3)
                        .urlId(URL_3)
                        .user(C_3)
                        .tokenHash(passwordEncoder.encode(TOKEN_3))
                        .createdAt(now.minusDays(2))
                        .expiresAt(now.plusDays(5))
                        .remainingUses(1)
                        .scope("sign-engagement-letter")
                        .documentId(ID_3)
                        .build()
        );
        this.accessLinkRepository.saveAll(accessLinks);
        log.warn("        ------- accessLinks");

        List<DataProcessingConsent> dataProcessingConsents = List.of(
                DataProcessingConsent.builder()
                        .id(ID_0)
                        .signatureAt(LocalDateTime.now().minusDays(10))
                        .signer(C_0)
                        .signerFullName(C_0.fullName())
                        .signerIdentity(C_0.getIdentity())
                        .mobile(C_0.getMobile())
                        .signerEmail(C_0.getEmail())
                        .signatureToken("consent-token-0001")
                        .deviceInfo(DeviceInfo.builder()
                                .ipAddress("83.52.10.24")
                                .browser("Chrome")
                                .operatingSystem("Windows")
                                .deviceType("Desktop")
                                .build())
                        .policyVersion("2026-02-19")
                        .dataProcessingAccepted(true)
                        .promotionsAccepted(true)
                        .build(),

                DataProcessingConsent.builder()
                        .id(ID_1)
                        .signatureAt(LocalDateTime.now().minusDays(3))
                        .signer(C_1)
                        .signerFullName(C_1.fullName())
                        .signerIdentity(C_1.getIdentity())
                        .mobile(C_1.getMobile())
                        .signerEmail(C_1.getEmail())
                        .signatureToken("consent-token-0002")
                        .deviceInfo(deviceInfo)
                        .policyVersion("2026-04-25")
                        .dataProcessingAccepted(true)
                        .promotionsAccepted(false)
                        .build(),

                DataProcessingConsent.builder()
                        .id(ID_2)
                        .signatureAt(LocalDateTime.now().minusHours(6))
                        .signer(C_2)
                        .signerFullName(C_2.fullName())
                        .signerIdentity(C_2.getIdentity())
                        .mobile(C_2.getMobile())
                        .signerEmail(C_2.getEmail())
                        .signatureToken("consent-token-0003")
                        .deviceInfo(deviceInfo)
                        .policyVersion("2026-04-25")
                        .dataProcessingAccepted(true)
                        .promotionsAccepted(false)
                        .build(),

                DataProcessingConsent.builder()
                        .id(ID_3)
                        .signatureAt(LocalDateTime.now().minusMinutes(20))
                        .signer(C_3)
                        .signerFullName(C_3.fullName())
                        .signerIdentity(C_3.getIdentity())
                        .mobile(C_3.getMobile())
                        .signerEmail(C_3.getEmail())
                        .signatureToken("consent-token-0004")
                        .deviceInfo(deviceInfo)
                        .policyVersion("2026-04-25")
                        .dataProcessingAccepted(true)
                        .promotionsAccepted(true)
                        .build()
        );
        this.dataProcessingConsentRepository.saveAll(dataProcessingConsents);
        log.warn("        ------- dataProcessingConsents");
    }

    private User withEncodedPassword(User user, String pass) {
        user.setPassword(pass);
        return user;
    }

}
