package com.lld.patterns.visitor.hotel;

/** Element: rooms accept a visitor (first dispatch). */
public interface IRoom {
    void accept(IRoomVisitor visitor);
}
