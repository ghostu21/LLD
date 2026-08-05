package com.spotify.lld.license;

import java.time.Instant;
import java.util.Set;

/**
 * Legal rights window for a single track.
 * <p>
 * Why: labels license by territory and calendar window. Streaming without a
 * valid license is a lawsuit risk, not a UX bug.
 * <p>
 * Logic: {@link #isAvailableIn} is true only when country ∈ allowedCountries
 * AND now ∈ (validFrom, validUntil).
 */
public class License {
    private final String trackId;
    /** ISO country codes where this track may be streamed. */
    private final Set<String> allowedCountries;
    private final Instant validFrom;
    private final Instant validUntil;

    public License(String trackId, Set<String> allowedCountries,
                   Instant validFrom, Instant validUntil) {
        this.trackId = trackId;
        this.allowedCountries = allowedCountries;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    /**
     * Geo + time check for a play attempt.
     * Example: allowed={US}, now in window → US OK, IN blocked.
     */
    public boolean isAvailableIn(String countryCode) {
        Instant now = Instant.now();
        return allowedCountries.contains(countryCode)
                && now.isAfter(validFrom)
                && now.isBefore(validUntil);
    }

    public String getTrackId() { return trackId; }
}
