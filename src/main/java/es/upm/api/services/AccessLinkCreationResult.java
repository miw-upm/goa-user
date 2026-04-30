package es.upm.api.services;

import es.upm.api.data.entities.AccessLink;

public record AccessLinkCreationResult(AccessLink accessLink, String token) {
}
