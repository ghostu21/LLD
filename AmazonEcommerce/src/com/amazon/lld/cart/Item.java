package com.amazon.lld.cart;

/**
 * Line item in a shopping cart (product reference + quantity + captured unit price).
 * <p>
 * Why: unit price is snapshotted at add-time so catalog price changes do not
 * alter an in-progress cart total unexpectedly.
 */
public class Item {
    private final String productId;
    private int quantity;
    private final double unitPrice;

    /**
     * @param productId  catalog product id
     * @param quantity   units (must be positive)
     * @param unitPrice  price at time of add
     */
    public Item(String productId, int quantity, double unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** @return product id */
    public String getProductId() { return productId; }

    /** @return quantity */
    public int getQuantity() { return quantity; }

    /** @return snapshotted unit price */
    public double getUnitPrice() { return unitPrice; }

    /**
     * Updates line quantity.
     *
     * @param newQuantity must be positive
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity = newQuantity;
    }

    /** @return line total (unitPrice × quantity) */
    public double lineTotal() {
        return unitPrice * quantity;
    }
}
