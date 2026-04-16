package es.upm.api.data.daos;

import es.upm.api.data.entities.AccessLink;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AccessLinkRepositoryCustom {
    List<AccessLink> findActiveUsedAndByUserIdAndByScope(LocalDateTime now, UUID userId, String scope);
}
