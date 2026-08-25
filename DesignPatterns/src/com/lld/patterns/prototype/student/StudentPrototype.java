package com.lld.patterns.prototype.student;

/** Prototype: the object knows how to copy itself (including private fields). */
public interface StudentPrototype {
    StudentPrototype clone();
}
