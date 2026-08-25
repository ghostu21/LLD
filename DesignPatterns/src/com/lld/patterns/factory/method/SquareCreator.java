package com.lld.patterns.factory.method;

import com.lld.patterns.factory.shape.Shape;
import com.lld.patterns.factory.shape.Square;

public class SquareCreator extends ShapeFactory {
    @Override
    public Shape createShape() {
        return new Square();
    }
}
