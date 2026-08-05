package com.spotify.lld.license;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LicenseService {
    private final Map<String, License> licenses = new ConcurrentHashMap<>();

    public void registerLicense(License license) {
        licenses.put(license.getTrackId(), license);
    }

    public boolean canPlay(String trackId, String countryCode) {
        License license = licenses.get(trackId);
        return license != null && license.isAvailableIn(countryCode);
    }

    public void assertPlayable(String trackId, String countryCode) {
        if (!canPlay(trackId, countryCode)) {
            throw new LicenseException(
                    "Track " + trackId + " is not available in " + countryCode);
        }
    }
}
