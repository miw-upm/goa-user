package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccessLinkRepositoryCustomImpl implements AccessLinkRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<AccessLink> searchActiveUsedByUserIdsAndScope(List<UUID> userIds, String scope) {
        Query query = new Query();
        query.addCriteria(Criteria.where("expiresAt").gte(LocalDateTime.now()));
        if (userIds != null && !userIds.isEmpty()) {
            query.addCriteria(Criteria.where("user.$id").in(userIds));
        }
        if (StringUtils.hasText(scope)) {
            query.addCriteria(Criteria.where("scope").regex(scope, "i"));
        }
        return mongoTemplate.find(query, AccessLink.class);
    }

    @Override
    public List<AccessLink> searchExpiredUnusedByUserIdsAndScope(List<UUID> userIds, String scope) {
        Query query = new Query();
        query.addCriteria(Criteria.where("expiresAt").lt(LocalDateTime.now()));
        query.addCriteria(Criteria.where("lastUsedForUpdateAt").is(null));
        if (userIds != null && !userIds.isEmpty()) {
            query.addCriteria(Criteria.where("user.$id").in(userIds));
        }
        if (StringUtils.hasText(scope)) {
            query.addCriteria(Criteria.where("scope").regex(scope, "i"));
        }
        return mongoTemplate.find(query, AccessLink.class);
    }
}