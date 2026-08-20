package com.lld.patterns.chainofresponsibility.logging;

public class InfoLogProcessor extends LogProcessor {
    public InfoLogProcessor(int level) {
        this.level = level;
    }

    @Override
    protected void write(String message) {
        System.out.println("INFO: " + message);
    }
}
