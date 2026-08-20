package com.lld.patterns.chainofresponsibility.atm;

public class Rupee100Dispenser extends DispenseHandler {
    @Override
    protected int noteValue() {
        return 100;
    }
}
