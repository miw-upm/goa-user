package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;

import java.util.Collection;
import java.util.List;

public interface UserRepositoryCustom {
    List<User> findByMobileAndFirstNameAndFamilyNameContainingNullSafe(String mobile, String firstName, String familyName,
                                                                        Collection<Role> roles);

    List<User> findByAll(String attribute, Collection<Role> roles);
}
