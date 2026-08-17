package com.hotel.lld.billing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Itemized stay bill. Total = sum of line items (refunds are negative).
 */
public class Bill {
    private final List<BillItem> items = new ArrayList<>();
    private boolean paid;

    public void addItem(BillItem item) {
        items.add(item);
    }

    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public double getTotal() {
        return items.stream().mapToDouble(BillItem::getAmount).sum();
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
