package com.lld.patterns.factory.demo;

import com.lld.patterns.factory.method.CircleCreator;
import com.lld.patterns.factory.method.RectangleCreator;
import com.lld.patterns.factory.method.ShapeFactory;
import com.lld.patterns.factory.method.SquareCreator;
import com.lld.patterns.factory.shape.Shape;
import com.lld.patterns.factory.shape.ShapeType;

public class FactoryPatternDemo {
    public static void main(String[] args) {
        runSimpleFactory();
        runFactoryMethod();
    }

    private static void runSimpleFactory() {
        System.out.println("======= Simple Factory Design Pattern ======");
        ShapeType shapeType = ShapeType.SQUARE;
        Shape shape = com.lld.patterns.factory.simple.ShapeFactory.createShapeInstance(shapeType);
        shape.draw();
        shape.computeArea();
        System.out.println();
    }

    private static void runFactoryMethod() {
        System.out.println("======= Factory Method Design Pattern ======");

        ShapeFactory creator = new SquareCreator();
        Shape shape = creator.createShape();
        shape.draw();
        shape.computeArea();

        System.out.println("-- via ShapeType (note's client switch) --");
        Shape fromType = getShapeInstance(ShapeType.CIRCLE);
        fromType.draw();
        fromType.computeArea();
        System.out.println();
    }

    /**
     * The LLD note still switches on type to pick a creator. That wiring is OCP-weak;
     * each {@code *Creator} is OCP-clean. Prefer injecting {@link ShapeFactory}.
     */
    private static Shape getShapeInstance(ShapeType shapeType) {
        if (shapeType == null) {
            return null;
        }
        ShapeFactory factory;
        switch (shapeType) {
            case CIRCLE:
                factory = new CircleCreator();
                break;
            case RECTANGLE:
                factory = new RectangleCreator();
                break;
            case SQUARE:
                factory = new SquareCreator();
                break;
            default:
                throw new IllegalStateException("ShapeType doesn't exist.");
        }
        return factory.createShape();
    }
}
