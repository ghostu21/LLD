package com.lld.patterns.factory.method;

import com.lld.patterns.factory.shape.Rectangle;
import com.lld.patterns.factory.shape.Shape;

public class RectangleCreator extends ShapeFactory {
    @Override
    public Shape createShape() {
        return new Rectangle();
    }
}
