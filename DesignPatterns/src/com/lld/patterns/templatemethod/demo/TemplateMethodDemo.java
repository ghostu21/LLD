package com.lld.patterns.templatemethod.demo;

import com.lld.patterns.templatemethod.payment.BankTransfer;
import com.lld.patterns.templatemethod.payment.MerchantPayment;
import com.lld.patterns.templatemethod.payment.PaymentFlow;

public class TemplateMethodDemo {
    public static void main(String[] args) {
        System.out.println("###### Template Method Design Pattern ######");

        System.out.println("===== Bank Transfer =====");
        PaymentFlow bankTransfer = new BankTransfer();
        bankTransfer.sendMoney();
        bankTransfer.logTransaction();

        System.out.println("===== Merchant Payment =====");
        PaymentFlow merchantPayment = new MerchantPayment();
        merchantPayment.sendMoney();
        merchantPayment.logTransaction();
    }
}
