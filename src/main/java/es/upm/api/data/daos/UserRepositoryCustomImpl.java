package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<User> findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(String mobile, String firstName, String familyName,
                                                                                            String email, String identity, Collection<Role> roles) {
        Criteria criteria = new Criteria();

        if (StringUtils.hasText(mobile))
            criteria.and("mobile").regex(mobile, "i");
        if (StringUtils.hasText(firstName))
            criteria.and("firstName").regex(firstName, "i");
        if (StringUtils.hasText(familyName))
            criteria.and("familyName").regex(familyName, "i");
        if (StringUtils.hasText(email))
            criteria.and("email").regex(email, "i");
        if (StringUtils.hasText(identity))
            criteria.and("identity").regex(identity, "i");

        criteria.and("role").in(roles);

        return mongoTemplate.find(Query.query(criteria), User.class);
    }

    @Override
    public List<User> findByAll(String attribute, Collection<Role> roles) {
        Criteria criteria = new Criteria();

        if (StringUtils.hasText(attribute)) {
            criteria.orOperator(
                    Criteria.where("mobile").regex(attribute, "i"),
                    Criteria.where("firstName").regex(attribute, "i"),
                    Criteria.where("familyName").regex(attribute, "i"),
                    Criteria.where("email").regex(attribute, "i"),
                    Criteria.where("identity").regex(attribute, "i")
            );
        }

        criteria.and("role").in(roles);

        return mongoTemplate.find(Query.query(criteria), User.class);
    }
}
