package com.lld.patterns.factory.method;

import com.lld.patterns.factory.shape.Shape;

/**
 * Creator: subclasses implement {@link #createShape()} (the factory method).
 * The note also names this {@code getShapeInstance} on concrete creators — same method.
 */
public abstract class ShapeFactory {
    public abstract Shape createShape();
}
