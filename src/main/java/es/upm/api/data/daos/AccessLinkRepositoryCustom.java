package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;

import java.util.List;
import java.util.UUID;

public interface AccessLinkRepositoryCustom {
    List<AccessLink> findActiveUsedByUserIdsAndScope(List<UUID> userIds, String scope);

    List<AccessLink> findExpiredUnusedByUserIdsAndScope(List<UUID> userIds, String scope);
}
