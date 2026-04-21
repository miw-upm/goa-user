package es.upm.api.configurations;

import es.upm.api.data.daos.AccessLinkRepository;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.*;
import es.upm.miw.uuid.UUIDBase64;
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

    private final UserRepository userRepository;
    private final AccessLinkRepository accessLinkRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.db.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        this.deleteAll();
        this.seed();
    }

    private void deleteAll() {
        this.accessLinkRepository.deleteAll();
        this.userRepository.deleteAll();
        log.warn("------- Deleted All -----------");
    }

    private void seed() {
        log.warn("------- Initial Load from JAVA -----------");
        String pass = this.passwordEncoder.encode(this.password);
        String noPass = this.passwordEncoder.encode(UUIDBase64.URL.encode());

        List<User> users = List.of(
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0666"))
                        .mobile("6").firstName("admin").password(pass).role(Role.ADMIN)
                        .registrationDate(LocalDate.now()).active(true).build(),
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
                        .city("Madrid").province(Province.MADRID).postalCode(28012)
                        .role(Role.CUSTOMER).registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"))
                        .mobile("666666001").firstName("c2").familyName("family-c2").password(noPass)
                        .documentType(DocumentType.DNI).identity("66666604T").address("C/TPV, 4").email("c2@gmail.com")
                        .city("Sevilla").province(Province.SEVILLA).postalCode(41001)
                        .role(Role.CUSTOMER).registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0006"))
                        .mobile("666666002").firstName("c3").password(noPass).role(Role.CUSTOMER)
                        .city("Cádiz").province(Province.CADIZ).postalCode(11001)
                        .registrationDate(LocalDate.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0007"))
                        .mobile("666666003").firstName("admin3").password(pass).role(Role.ADMIN)
                        .registrationDate(LocalDate.now()).active(true).build()
        );
        this.userRepository.saveAll(users);
        log.warn("        ------- users");

        User c1 = users.get(3);
        User c2 = users.get(4);

        List<AccessLink> accessLinks = List.of(
                AccessLink.builder().id("GiTBDnRkS-aNYOayM69_kA").user(c2).createdAt(LocalDateTime.now())
                        .lastUsedForUpdateAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(5)).remainingUses(4).scope("edit-profile").build(),
                AccessLink.builder().id("XWBLFua2T6GLVh5wqKHB8w").createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(10)).user(c1).remainingUses(2).scope("accept-engagement")
                        .document(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0007")).build(),
                AccessLink.builder().id("hNSvhWOmQH6-NNo3gXnyow").createdAt(LocalDateTime.now().minusDays(10))
                        .expiresAt(LocalDateTime.now().minusDays(5)).user(c1).remainingUses(4).scope("edit-profile").build(),
                AccessLink.builder().id("6JuwxpWVSiuv90nxgfwKmA").createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(5)).user(c1).remainingUses(0).scope("edit-profile").build()
        );
        this.accessLinkRepository.saveAll(accessLinks);
        log.warn("        ------- accessLinks");
    }

}
