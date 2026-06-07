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
    public List<User> findUsers(String customer, Boolean active, Collection<Role> roles) {
        List<Criteria> criteria = new ArrayList<>();
        criteria.add(Criteria.where("role").in(roles));
        if (active != null) {
            criteria.add(Criteria.where("active").is(active));
        }
        criteria.addAll(this.customerTextCriteria(customer));
        return mongoTemplate.find(
                Query.query(new Criteria().andOperator(criteria.toArray(Criteria[]::new))),
                User.class
        );
    }

    @Override
    public List<User> findCustomersByText(String text, Collection<Role> roles) {
        List<Criteria> criteria = new ArrayList<>();
        criteria.add(Criteria.where("role").in(roles));
        criteria.addAll(this.customerTextCriteria(text));
        return mongoTemplate.find(
                Query.query(new Criteria().andOperator(criteria.toArray(Criteria[]::new))),
                User.class
        );
    }

    private List<Criteria> customerTextCriteria(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return Arrays.stream(text.trim().split("\\s+"))
                .filter(StringUtils::hasText)
                .map(Pattern::quote)
                .map(term -> new Criteria().orOperator(
                        Criteria.where("mobile").regex(term, "i"),
                        Criteria.where("firstName").regex(term, "i"),
                        Criteria.where("familyName").regex(term, "i")
                ))
                .toList();
    }
}
