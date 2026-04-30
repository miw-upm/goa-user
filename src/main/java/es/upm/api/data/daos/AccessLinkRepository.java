package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccessLinkRepository extends MongoRepository<AccessLink, UUID>, AccessLinkRepositoryCustom {
    Optional<AccessLink> findByUrlId(String urlId);
}
