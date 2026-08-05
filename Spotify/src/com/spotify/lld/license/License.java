package com.spotify.lld.license;

import java.time.Instant;
import java.util.Set;

public class License {
    private final String trackId;
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

    public boolean isAvailableIn(String countryCode) {
        Instant now = Instant.now();
        return allowedCountries.contains(countryCode)
                && now.isAfter(validFrom)
                && now.isBefore(validUntil);
    }

    public String getTrackId() { return trackId; }
}
