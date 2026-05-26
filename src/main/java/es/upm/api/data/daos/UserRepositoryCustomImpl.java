package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<User> findCustomers(String mobile, String firstName, String familyName, Collection<Role> roles) {
        Criteria criteria = new Criteria();
        if (StringUtils.hasText(mobile))
            criteria.and("mobile").regex(mobile, "i");
        if (StringUtils.hasText(firstName))
            criteria.and("firstName").regex(firstName, "i");
        if (StringUtils.hasText(familyName))
            criteria.and("familyName").regex(familyName, "i");
        criteria.and("role").in(roles);
        return mongoTemplate.find(Query.query(criteria), User.class);
    }

    @Override
    public List<User> findCustomersByText(String text, Collection<Role> roles) {
        List<Criteria> criteria = new ArrayList<>();
        criteria.add(Criteria.where("role").in(roles));
        if (StringUtils.hasText(text)) {
            Arrays.stream(text.trim().split("\\s+"))
                    .filter(StringUtils::hasText)
                    .map(Pattern::quote)
                    .map(term -> new Criteria().orOperator(
                            Criteria.where("mobile").regex(term, "i"),
                            Criteria.where("firstName").regex(term, "i"),
                            Criteria.where("familyName").regex(term, "i")
                    ))
                    .forEach(criteria::add);
        }
        return mongoTemplate.find(
                Query.query(new Criteria().andOperator(criteria.toArray(Criteria[]::new))),
                User.class
        );
    }
}
