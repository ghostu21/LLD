package com.amazon.lld.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory product catalog with Trie name search and category index.
 * <p>
 * Why: ConcurrentHashMap gives O(1) lookup by id; {@link ProductSearchIndex}
 * avoids scanning all products for text search; category map supports browse filters.
 * <p>
 * Logic: {@link #addProduct} stores product, indexes name in Trie, and buckets
 * by category. Search methods resolve ids to live Product references.
 */
public class ProductCatalog {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final ProductSearchIndex nameIndex = new ProductSearchIndex();
    private final Map<ProductCategoryType, List<String>> categoryIndex = new ConcurrentHashMap<>();

    /**
     * Adds a product and updates search indexes.
     *
     * @param product new or updated listing
     */
    public void addProduct(Product product) {
        products.put(product.getId(), product);
        nameIndex.insert(product.getName(), product.getId());
        categoryIndex.computeIfAbsent(product.getCategory(), k -> new ArrayList<>())
                .add(product.getId());
    }

    /**
     * @param productId product id
     * @return product or null
     */
    public Product getProduct(String productId) {
        return products.get(productId);
    }

    /**
     * Trie-backed name search (not a list scan).
     *
     * @param query partial name
     * @return matching products
     */
    public List<Product> searchByName(String query) {
        return resolveIds(nameIndex.search(query));
    }

    /**
     * Category filter via secondary index.
     *
     * @param category category enum
     * @return products in category
     */
    public List<Product> searchByCategory(ProductCategoryType category) {
        List<String> ids = categoryIndex.getOrDefault(category, List.of());
        return resolveIds(ids);
    }

    /** @return all products (unmodifiable list) */
    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(new ArrayList<>(products.values()));
    }

    private List<Product> resolveIds(Iterable<String> ids) {
        List<Product> result = new ArrayList<>();
        for (String id : ids) {
            Product p = products.get(id);
            if (p != null) result.add(p);
        }
        return result;
    }
}
