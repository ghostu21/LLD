package com.lld.patterns.facade.order;

public class NotificationService {
    public void sendConfirmation(String productId) {
        System.out.println("Sending order confirmation for product: " + productId);
    }
}
