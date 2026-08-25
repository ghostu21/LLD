package com.lld.patterns.factory.simple;

import com.lld.patterns.factory.shape.Circle;
import com.lld.patterns.factory.shape.Rectangle;
import com.lld.patterns.factory.shape.Shape;
import com.lld.patterns.factory.shape.ShapeType;
import com.lld.patterns.factory.shape.Square;

/**
 * Simple Factory (idiom, not GoF): one static method, {@code switch} on type.
 * Adding {@code TRIANGLE} means editing this class (OCP).
 */
public class ShapeFactory {
    public static Shape createShapeInstance(ShapeType shapeType) {
        if (shapeType == null) {
            return null;
        }
        switch (shapeType) {
            case CIRCLE:
                return new Circle();
            case RECTANGLE:
                return new Rectangle();
            case SQUARE:
                return new Square();
            default:
                throw new IllegalStateException("ShapeType doesn't exist!");
        }
    }
}
