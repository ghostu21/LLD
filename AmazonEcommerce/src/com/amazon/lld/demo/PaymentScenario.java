package com.amazon.lld.demo;

import com.amazon.lld.payment.Payment;
import com.amazon.lld.payment.PaymentMethodType;
import com.amazon.lld.payment.PaymentProcessor;
import com.amazon.lld.payment.PaymentStatus;

/**
 * Demo: Strategy + Factory payment (credit card vs bank transfer).
 * <p>
 * Interview angle: no hardcoded Payment subclasses — PaymentStrategyFactory.
 */
public class PaymentScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Payment Strategy + Factory ---");
        PaymentProcessor processor = new PaymentProcessor();

        Payment cc = processor.process(99.99, PaymentMethodType.CREDIT_CARD);
        System.out.println("Credit card: " + cc.getStatus() + " txn=" + cc.getTransactionId());

        Payment ach = processor.process(49.99, PaymentMethodType.BANK_TRANSFER);
        System.out.println("Bank transfer: " + ach.getStatus() + " txn=" + ach.getTransactionId());
        System.out.println("Both COMPLETED: " +
                (cc.getStatus() == PaymentStatus.COMPLETED && ach.getStatus() == PaymentStatus.COMPLETED));
    }
}
