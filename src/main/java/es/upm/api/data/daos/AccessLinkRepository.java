package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AccessLinkRepository extends JpaRepository<AccessLink, String> {
    @Modifying
    @Query("delete from AccessLink a where a.id like concat('%', ?1)")
    void deleteByIdSuffix(String idSuffix);
}
