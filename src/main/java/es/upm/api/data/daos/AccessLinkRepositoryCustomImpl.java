package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccessLinkRepositoryCustomImpl implements AccessLinkRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<AccessLink> findActiveUsedAndByUserIdAndByScope(LocalDateTime now, UUID userId, String scope) {
        Query query = new Query();

        query.addCriteria(Criteria.where("expiresAt").gte(now));
        query.addCriteria(Criteria.where("lastUsedForUpdateAt").ne(null));

        if (userId != null) {
            query.addCriteria(Criteria.where("user.$id").is(userId));
        }
        if (scope != null) {
            query.addCriteria(Criteria.where("scope").is(scope));
        }
        return mongoTemplate.find(query, AccessLink.class);
    }
}
