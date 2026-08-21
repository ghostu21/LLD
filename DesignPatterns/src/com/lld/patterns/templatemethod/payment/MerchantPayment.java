package com.lld.patterns.templatemethod.payment;

public class MerchantPayment extends PaymentFlow {
    @Override
    public void validateRequest() {
        System.out.println("[+] Specific Validation Logic for Merchant Payment");
    }

    @Override
    public void debitAmount() {
        if (requiresOTPAuthentication()) {
            System.out.println("[+] Perform OTP Authentication.");
        }
        System.out.println("[+] Specific Debit Amount Logic for Merchant Payment");
    }

    @Override
    public void calculateFees() {
        System.out.println("[+] Specific Fee Calculation Logic for Merchant Payment. 2% Fees is applied.");
    }

    @Override
    public void creditAmount() {
        System.out.println("[+] Specific Credit Amount Logic for Merchant Payment. Remaining amount is credited.");
    }

    @Override
    protected boolean requiresOTPAuthentication() {
        return true;
    }
}
