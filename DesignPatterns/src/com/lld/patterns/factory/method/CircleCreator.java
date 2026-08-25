package com.lld.patterns.factory.method;

import com.lld.patterns.factory.shape.Circle;
import com.lld.patterns.factory.shape.Shape;

public class CircleCreator extends ShapeFactory {
    @Override
    public Shape createShape() {
        return new Circle();
    }
}
