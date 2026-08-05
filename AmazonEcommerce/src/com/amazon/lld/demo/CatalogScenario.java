package com.amazon.lld.demo;

import com.amazon.lld.catalog.ProductCategoryType;

/**
 * Demo: Trie-backed product search by name and category index.
 * <p>
 * Interview angle: O(k) search, not list scan.
 */
public class CatalogScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Catalog Search (Trie) ---");
        System.out.println("Search 'phone': " +
                fx.catalog.searchByName("phone").stream()
                        .map(p -> p.getName()).toList());
        System.out.println("Search 'code': " +
                fx.catalog.searchByName("code").stream()
                        .map(p -> p.getName()).toList());
        System.out.println("Category BOOKS: " +
                fx.catalog.searchByCategory(ProductCategoryType.BOOKS).stream()
                        .map(p -> p.getName()).toList());
    }
}
