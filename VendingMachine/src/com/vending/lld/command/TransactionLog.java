package com.vending.lld.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Append-only transaction log (refunds, collects, discounts).
 */
public final class TransactionLog {
    private final List<String> entries = new CopyOnWriteArrayList<>();

    public void record(PaymentCommand command) {
        entries.add(System.currentTimeMillis() + " " + command.description());
    }

    public void record(String line) {
        entries.add(System.currentTimeMillis() + " " + line);
    }

    public List<String> entries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }
}
