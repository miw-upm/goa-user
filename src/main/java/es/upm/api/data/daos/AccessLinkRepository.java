package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccessLinkRepository extends MongoRepository<AccessLink, String>, AccessLinkRepositoryCustom {
}
