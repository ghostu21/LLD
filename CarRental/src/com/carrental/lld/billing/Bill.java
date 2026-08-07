package com.carrental.lld.billing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Itemized bill with running total and payment status.
 * <p>
 * Why: reservations accumulate base rental, add-ons, fines, and taxes in one
 * place before payment processing.
 */
public class Bill {
    private final List<BillItem> items = new ArrayList<>();
    private double total;
    private PaymentStatus status;

    /** Creates an unpaid bill with zero total. */
    public Bill() {
        this.total = 0.0;
        this.status = PaymentStatus.UNPAID;
    }

    /**
     * Appends a line item and recalculates total.
     *
     * @param item new line
     */
    public void addItem(BillItem item) {
        items.add(item);
        recalculateTotal();
    }

    /** Re-sums all line amounts into {@link #total}. */
    private void recalculateTotal() {
        total = items.stream().mapToDouble(BillItem::getAmount).sum();
    }

    /** @return unmodifiable item list */
    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** @return bill total */
    public double getTotal() { return total; }

    /** @return payment status */
    public PaymentStatus getStatus() { return status; }

    /** @param status new payment status */
    public void setStatus(PaymentStatus status) { this.status = status; }
}
