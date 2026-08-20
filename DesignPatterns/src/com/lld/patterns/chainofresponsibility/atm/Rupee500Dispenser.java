package com.lld.patterns.chainofresponsibility.atm;

public class Rupee500Dispenser extends DispenseHandler {
    @Override
    protected int noteValue() {
        return 500;
    }
}
