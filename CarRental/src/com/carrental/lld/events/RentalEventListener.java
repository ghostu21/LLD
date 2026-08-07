package com.carrental.lld.events;

/**
 * Subscriber contract for rental events.
 */
@FunctionalInterface
public interface RentalEventListener {
    /**
     * Handles one published event.
     *
     * @param event domain event
     */
    void onEvent(RentalEvent event);
}
