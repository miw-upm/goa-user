package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLinkRepository extends JpaRepository<AccessLink, String> {
}
