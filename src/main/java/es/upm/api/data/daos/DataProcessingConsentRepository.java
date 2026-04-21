package es.upm.api.data.daos;

import es.upm.api.data.entities.DataProcessingConsent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface DataProcessingConsentRepository extends MongoRepository<DataProcessingConsent, UUID> {
    List<DataProcessingConsent> findByMobile(String mobile);
}
