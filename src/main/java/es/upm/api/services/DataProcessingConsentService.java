package es.upm.api.services;

import es.upm.api.data.entities.DataProcessingConsent;
import es.upm.miw.device.DeviceInfo;

import java.util.UUID;
import java.util.stream.Stream;


public class DataProcessingConsentService {

    public DataProcessingConsent read(UUID id) {
        return null;
    }

    public Stream<DataProcessingConsent> findNullSafe(DataProcessingConsentFindCriteria criteria) {
        return null;
    }

    public void create(String mobile, String token, DataProcessingConsentCreation consentCreation, DeviceInfo deviceInfo) {
    }
}
