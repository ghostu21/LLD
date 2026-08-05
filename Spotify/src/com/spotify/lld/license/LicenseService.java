package com.spotify.lld.license;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facade that registers licenses and gates playback by country.
 * <p>
 * Why: call this before starting a stream so geo-blocked / expired tracks
 * never enter the player pipeline.
 * <p>
 * Logic: map trackId → License; {@link #canPlay} soft-check;
 * {@link #assertPlayable} hard-check that throws {@link LicenseException}.
 */
public class LicenseService {
    private final Map<String, License> licenses = new ConcurrentHashMap<>();

    /** Upserts the license record for a track. */
    public void registerLicense(License license) {
        licenses.put(license.getTrackId(), license);
    }

    /**
     * Soft check: false if missing license or country/time fails.
     */
    public boolean canPlay(String trackId, String countryCode) {
        License license = licenses.get(trackId);
        return license != null && license.isAvailableIn(countryCode);
    }

    /**
     * Hard gate used on the play path — throws when the track is not playable
     * in {@code countryCode} right now.
     */
    public void assertPlayable(String trackId, String countryCode) {
        if (!canPlay(trackId, countryCode)) {
            throw new LicenseException(
                    "Track " + trackId + " is not available in " + countryCode);
        }
    }
}
