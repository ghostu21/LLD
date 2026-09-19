package com.gdrive.lld.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe subject: attach/detach/notify without {@code ConcurrentModificationException}.
 * <p>
 * Why: shares are read often and mutated rarely — COW list is the right default.
 */
public class Subject {
    private final List<DriveObserver> observers = new CopyOnWriteArrayList<>();

    public void attach(DriveObserver observer) {
        observers.add(observer);
    }

    public void detach(DriveObserver observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(String message) {
        for (DriveObserver observer : observers) {
            observer.update(message);
        }
    }
}
