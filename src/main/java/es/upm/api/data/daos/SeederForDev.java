package es.upm.api.data.daos;

import es.upm.api.data.entities.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Log4j2
@Repository
@Profile({"dev", "test"})
public class SeederForDev {
    private final String pass;
    private final String noPass;
    private final DatabaseStarting databaseStarting;
    private final UserRepository userRepository;
    private final AccessLinkRepository accessLinkRepository;

    @Autowired
    public SeederForDev(DatabaseStarting databaseStarting, @Value("${miw.password}") String password,
                        PasswordEncoder passwordEncoder, UserRepository userRepository, AccessLinkRepository accessLinkRepository) {
        this.databaseStarting = databaseStarting;
        this.pass = passwordEncoder.encode(password);
        this.noPass = passwordEncoder.encode(UUIDBase64.URL.encode());
        this.userRepository = userRepository;
        this.accessLinkRepository = accessLinkRepository;

        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBase();
    }

    public void deleteAllAndInitialize() {
        this.accessLinkRepository.deleteAll();
        this.userRepository.deleteAll();
        log.warn("------- Deleted All -----------");
        this.databaseStarting.initialize();
    }

    private void seedDataBase() {
        log.warn("------- Initial Load from JAVA -----------");
        User[] users = {
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000"))
                        .mobile("61").firstName("manager").password(pass).role(Role.MANAGER)
                        .registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001"))
                        .mobile("62").firstName("operator").password(pass).role(Role.OPERATOR)
                        .registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0002"))
                        .mobile("66").firstName("customer").password(pass).role(Role.CUSTOMER)
                        .registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004"))
                        .mobile("666666000").firstName("c1").familyName("family-c1").password(noPass)
                        .documentType(DocumentType.DNI).identity("66666603E").address("C/TPV, 3").email("c1@gmail.com")
                        .role(Role.CUSTOMER).registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"))
                        .mobile("666666001").firstName("c2").familyName("family-c2").password(noPass)
                        .documentType(DocumentType.DNI).identity("66666604T").address("C/TPV, 4").email("c2@gmail.com")
                        .role(Role.CUSTOMER).registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0006"))
                        .mobile("666666002").firstName("c3").password(noPass).role(Role.CUSTOMER)
                        .registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0007"))
                        .mobile("666666003").firstName("admin3").password(pass).role(Role.ADMIN)
                        .registrationDate(LocalDate.now()).active(true).build()
        };
        this.userRepository.saveAll(Arrays.asList(users));
        log.warn("        ------- users");

        AccessLink[] accessLinks = {
                AccessLink.builder().id("GiTBDnRkS-aNYOayM69_kA").user(users[4]).createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(5)).remainingUses(4).scope("EDIT_PROFILE").build(),
                AccessLink.builder().id("XWBLFua2T6GLVh5wqKHB8w").createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(5)).user(users[3]).remainingUses(4).scope("VIEW_INVOICE").build(),
                AccessLink.builder().id("hNSvhWOmQH6-NNo3gXnyow").createdAt(LocalDateTime.now().minusDays(10))
                        .expiresAt(LocalDateTime.now().minusDays(5)).user(users[3]).remainingUses(4).scope("EXPIRED").build(),
                AccessLink.builder().id("6JuwxpWVSiuv90nxgfwKmA").createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(5)).user(users[3]).remainingUses(0).scope("USED").build(),
        };
        this.accessLinkRepository.saveAll(Arrays.asList(accessLinks));
        log.warn("        ------- accessLinks");
    }

}
