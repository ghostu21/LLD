package com.amazon.lld.catalog;

import java.util.UUID;

/**
 * Sellable product in the marketplace catalog.
 * <p>
 * Why: immutable identity with mutable price/stock — sellers adjust listing
 * details without changing product id.
 * <p>
 * Logic: {@link #updatePrice} and {@link #adjustStock} are synchronized
 * so concurrent order and inventory updates stay consistent.
 */
public class Product {
    private final String id;
    private final String name;
    private final String description;
    private final ProductCategoryType category;
    private final String sellerId;
    private double price;
    private int stockCount;

    /**
     * @param name        product title
     * @param description listing description
     * @param price       unit price
     * @param category    browse category
     * @param stockCount  available units
     * @param sellerId    owning seller account id
     */
    public Product(String name, String description, double price,
                   ProductCategoryType category, int stockCount, String sellerId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockCount = stockCount;
        this.sellerId = sellerId;
    }

    /** @return product id */
    public String getId() { return id; }

    /** @return product name */
    public String getName() { return name; }

    /** @return description */
    public String getDescription() { return description; }

    /** @return unit price */
    public double getPrice() { return price; }

    /** @return category */
    public ProductCategoryType getCategory() { return category; }

    /** @return available stock */
    public int getStockCount() { return stockCount; }

    /** @return seller account id */
    public String getSellerId() { return sellerId; }

    /**
     * Updates listing price (seller action).
     *
     * @param newPrice must be positive
     */
    public synchronized void updatePrice(double newPrice) {
        if (newPrice <= 0) throw new IllegalArgumentException("Price must be positive");
        this.price = newPrice;
    }

    /**
     * Adjusts stock by delta (negative reduces stock on sale).
     *
     * @param delta units to add (positive) or remove (negative)
     */
    public synchronized void adjustStock(int delta) {
        int next = stockCount + delta;
        if (next < 0) throw new IllegalStateException("Insufficient stock");
        this.stockCount = next;
    }
}
