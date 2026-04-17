package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;

import java.util.List;
import java.util.UUID;

public interface AccessLinkRepositoryCustom {
    List<AccessLink> findActiveUsedAndByUserIdAndByScope(UUID userId, String scope);
}
