package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AccessLinkRepository extends MongoRepository<AccessLink, String> {
    @Query("{ _id: { $regex: ?0, $options: 'i' } }")
    List<AccessLink> read(String idSuffix);

    @Query("{ _id: { $regex: ?0, $options: 'i' } }")
    @DeleteQuery
    void deleteByIdSuffix(String idSuffix);
}
