package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;

import java.util.List;
import java.util.UUID;

public interface AccessLinkRepositoryCustom {
    List<AccessLink> searchActiveUsedByUserIdsAndScope(List<UUID> userIds, String scope);

    List<AccessLink> searchExpiredUnusedByUserIdsAndScope(List<UUID> userIds, String scope);
}
