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

    public synchronized void addItem(BillItem item) {
        items.add(item);
    }

    public synchronized List<BillItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized double getTotal() {
        return items.stream().mapToDouble(BillItem::getAmount).sum();
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
