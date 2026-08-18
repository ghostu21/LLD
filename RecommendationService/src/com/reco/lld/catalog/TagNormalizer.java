package com.reco.lld.catalog;

import java.util.Locale;

/**
 * Canonical tag form used by both catalog items and User Service preferences.
 * <p>
 * Why: "Software" vs "software" must match; rejecting control characters
 * stops junk tags from becoming a scoring feature.
 */
public final class TagNormalizer {
    private TagNormalizer() {}

    public static String normalize(String raw) {
        if (raw == null) return null;
        String t = raw.trim().toLowerCase(Locale.ROOT);
        return t.isEmpty() ? null : t;
    }

    public static boolean isLegalShape(String tag) {
        return tag != null && tag.length() <= 32 && tag.matches("[a-z0-9][a-z0-9-]{0,31}");
    }
}
