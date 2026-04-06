package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<User, UUID> {

    Optional<User> findByMobile(String mobile);

    List<User> findByRoleIn(Collection<Role> roles);

    boolean existsByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByIdentity(String identity);

    @Query("""
    {$and: [
        ?#{ [0] == null ? {_id: {$ne: null}} : {mobile: {$regex: [0], $options: 'i'}} },
        ?#{ [1] == null ? {_id: {$ne: null}} : {firstName: {$regex: [1], $options: 'i'}} },
        ?#{ [2] == null ? {_id: {$ne: null}} : {familyName: {$regex: [2], $options: 'i'}} },
        ?#{ [3] == null ? {_id: {$ne: null}} : {email: {$regex: [3], $options: 'i'}} },
        ?#{ [4] == null ? {_id: {$ne: null}} : {identity: {$regex: [4], $options: 'i'}} },
        {role: {$in: [5]}}
    ]}
    """)
    List<User> findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
            String mobile, String firstName, String familyName, String email, String identity, Collection<Role> roles);

    @Query("""
    {$and: [
        {$or: [
            {mobile:     {$regex: ?0, $options: 'i'}},
            {firstName:  {$regex: ?0, $options: 'i'}},
            {familyName: {$regex: ?0, $options: 'i'}},
            {email:      {$regex: ?0, $options: 'i'}},
            {identity:   {$regex: ?0, $options: 'i'}}
        ]},
        {role: {$in: ?1}}
    ]}
    """)
    List<User> findByAll(String attribute, Collection<Role> roles);
}
