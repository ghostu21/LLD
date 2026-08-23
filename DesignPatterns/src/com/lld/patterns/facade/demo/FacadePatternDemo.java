package com.lld.patterns.facade.demo;

import com.lld.patterns.facade.order.OrderFacade;

public class FacadePatternDemo {
    public static void main(String[] args) {
        System.out.println("====== Facade Design Pattern Demo ======");
        OrderFacade orderFacade = new OrderFacade();
        orderFacade.placeOrder("MacBook Pro", "Credit Card");
        System.out.println();
        orderFacade.placeOrder("Cricket Bat", "UPI");
    }
}
