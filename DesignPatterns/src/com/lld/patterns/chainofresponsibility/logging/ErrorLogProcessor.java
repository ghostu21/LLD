package com.lld.patterns.chainofresponsibility.logging;

public class ErrorLogProcessor extends LogProcessor {
    public ErrorLogProcessor(int level) {
        this.level = level;
    }

    @Override
    protected void write(String message) {
        System.out.println("ERROR: " + message);
    }
}
