package com.amazon.lld.command;

import com.amazon.lld.account.Address;
import com.amazon.lld.account.Member;
import com.amazon.lld.order.CheckoutService;
import com.amazon.lld.order.Order;
import com.amazon.lld.payment.PaymentMethodType;

/**
 * Command that delegates checkout to {@link CheckoutService}.
 * <p>
 * Why: uniform Command interface for "place order" in macro workflows.
 */
public class PlaceOrderCommand implements Command {
    private final CheckoutService checkoutService;
    private final Member member;
    private final PaymentMethodType methodType;
    private final Address address;
    private Order result;

    /**
     * @param checkoutService checkout orchestrator
     * @param member          buyer
     * @param methodType      payment rail
     * @param address         shipping address
     */
    public PlaceOrderCommand(CheckoutService checkoutService, Member member,
                             PaymentMethodType methodType, Address address) {
        this.checkoutService = checkoutService;
        this.member = member;
        this.methodType = methodType;
        this.address = address;
    }

    /** Runs checkout and stores result. */
    @Override
    public void execute() {
        result = checkoutService.checkout(member, methodType, address);
    }

    /** @return order created by last execute (null if not run) */
    public Order getResult() { return result; }
}
