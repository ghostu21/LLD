package com.lld.patterns.visitor.hotel;

/** Visitor: one visit method per room type (second dispatch). */
public interface IRoomVisitor {
    void visitStandardRoom(StandardRoom room);

    void visitDeluxeRoom(DeluxeRoom room);

    void visitSuiteRoom(SuiteRoom room);
}
