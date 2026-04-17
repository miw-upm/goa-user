package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<User, UUID>, UserRepositoryCustom {

    Optional<User> findByMobile(String mobile);

    List<User> findByRoleIn(Collection<Role> roles);

    boolean existsByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByIdentity(String identity);

    List<User> findByMobileContaining(String mobile);

}
