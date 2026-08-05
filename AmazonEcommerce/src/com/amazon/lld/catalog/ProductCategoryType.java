package com.amazon.lld.catalog;

/**
 * High-level product categories for browse and filter.
 * <p>
 * Why: category-based search complements name Trie lookup in
 * {@link ProductCatalog}.
 */
public enum ProductCategoryType {
    ELECTRONICS,
    BOOKS,
    CLOTHING,
    HOME,
    OTHER
}
