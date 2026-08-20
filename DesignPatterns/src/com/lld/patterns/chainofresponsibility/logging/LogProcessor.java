package com.lld.patterns.chainofresponsibility.logging;

/**
 * Handler: if this logger's level can handle the message, write, then always
 * forward to the next processor (propagating chain — not exclusive).
 */
public abstract class LogProcessor {
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int ERROR = 3;
    public static final int FATAL = 4;

    protected int level;
    protected LogProcessor nextLoggerProcessor;

    public void setNextLogger(LogProcessor nextLogger) {
        this.nextLoggerProcessor = nextLogger;
    }

    public void logMessage(int level, String message) {
        if (this.level <= level) {
            write(message);
        }
        if (this.nextLoggerProcessor != null) {
            this.nextLoggerProcessor.logMessage(level, message);
        }
    }

    protected abstract void write(String message);
}
