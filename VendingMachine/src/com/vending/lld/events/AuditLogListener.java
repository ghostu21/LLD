package com.vending.lld.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Audit / debug trail for admin mode.
 */
public final class AuditLogListener implements VendingEventListener {
    private final List<String> lines = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(VendingEvent event) {
        String line = event.getType() + " | " + event.getMessage();
        lines.add(line);
        System.out.println("Audit: " + line);
    }

    public List<String> lines() {
        return new ArrayList<>(lines);
    }
}
