package com.lld.patterns.facade.order;

public class PaymentService {
    public boolean makePayment(String paymentMethod) {
        System.out.println("Processing payment using: " + paymentMethod);
        return true;
    }
}
