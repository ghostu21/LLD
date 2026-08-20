package com.lld.patterns.observer.stock;

public interface StockNotificationObserver {
    void update();

    String getNotificationMethod();

    String getUserId();
}
