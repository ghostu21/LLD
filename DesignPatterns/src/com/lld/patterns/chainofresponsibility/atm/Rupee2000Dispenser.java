package com.lld.patterns.chainofresponsibility.atm;

public class Rupee2000Dispenser extends DispenseHandler {
    @Override
    protected int noteValue() {
        return 2000;
    }
}
