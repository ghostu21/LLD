package com.amazon.lld.events;

/**
 * Subscriber contract for {@link AsyncEventBus}.
 * <p>
 * Why: NotificationService and Member listeners implement this for async fan-out.
 */
public interface OrderEventListener {
    /**
     * Handles one published event (runs on bus worker thread).
     *
     * @param event order event
     */
    void onEvent(OrderEvent event);
}
