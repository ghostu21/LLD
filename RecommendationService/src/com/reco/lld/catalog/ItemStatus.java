package com.reco.lld.catalog;

/**
 * Catalog eligibility. Banned / out-of-stock items must never be recommended.
 */
public enum ItemStatus {
    ACTIVE,
    OUT_OF_STOCK,
    BANNED
}
