package com.lld.patterns.chainofresponsibility.demo;

import com.lld.patterns.chainofresponsibility.atm.DispenseHandler;
import com.lld.patterns.chainofresponsibility.atm.Rupee100Dispenser;
import com.lld.patterns.chainofresponsibility.atm.Rupee2000Dispenser;
import com.lld.patterns.chainofresponsibility.atm.Rupee500Dispenser;
import com.lld.patterns.chainofresponsibility.logging.DebugLogProcessor;
import com.lld.patterns.chainofresponsibility.logging.ErrorLogProcessor;
import com.lld.patterns.chainofresponsibility.logging.FatalLogProcessor;
import com.lld.patterns.chainofresponsibility.logging.InfoLogProcessor;
import com.lld.patterns.chainofresponsibility.logging.LogProcessor;

public class ChainOfResponsibilityDemo {
    public static void main(String[] args) {
        runLoggingDemo();
        runAtmDemo();
    }

    private static void runLoggingDemo() {
        System.out.println("###### Chain of Responsibility Design Pattern ######");
        System.out.println("###### Example: Logging System (propagating chain) ######");

        LogProcessor logProcessor = chainOfLoggers();

        System.out.println("===== Logging DEBUG message =====");
        logProcessor.logMessage(LogProcessor.DEBUG, "This is a debug message");
        System.out.println("===== Logging INFO message =====");
        logProcessor.logMessage(LogProcessor.INFO, "This is an info message");
        System.out.println("===== Logging ERROR message =====");
        logProcessor.logMessage(LogProcessor.ERROR, "This is an error message");
        System.out.println("===== Logging FATAL message =====");
        logProcessor.logMessage(LogProcessor.FATAL, "This is a fatal message");
        System.out.println();
    }

    private static LogProcessor chainOfLoggers() {
        LogProcessor fatalLogger = new FatalLogProcessor(LogProcessor.FATAL);
        LogProcessor errorLogger = new ErrorLogProcessor(LogProcessor.ERROR);
        LogProcessor infoLogger = new InfoLogProcessor(LogProcessor.INFO);
        LogProcessor debugLogger = new DebugLogProcessor(LogProcessor.DEBUG);

        debugLogger.setNextLogger(infoLogger);
        infoLogger.setNextLogger(errorLogger);
        errorLogger.setNextLogger(fatalLogger);
        return debugLogger;
    }

    private static void runAtmDemo() {
        System.out.println("###### Example: ATM (exclusive / remainder chain) ######");
        DispenseHandler atm = chainOfDispensers();
        System.out.println("===== Withdraw ₹3600 =====");
        atm.dispense(3600);
        System.out.println("===== Withdraw ₹700 =====");
        atm.dispense(700);
    }

    private static DispenseHandler chainOfDispensers() {
        DispenseHandler twoThousand = new Rupee2000Dispenser();
        DispenseHandler fiveHundred = new Rupee500Dispenser();
        DispenseHandler hundred = new Rupee100Dispenser();
        twoThousand.setNext(fiveHundred);
        fiveHundred.setNext(hundred);
        return twoThousand;
    }
}
