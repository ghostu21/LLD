package com.lld.patterns.factory.shape;

public class Circle implements Shape {
    @Override
    public void computeArea() {
        System.out.println("Inside Circle::computeArea() method.");
    }

    @Override
    public void draw() {
        System.out.println("Inside Circle::draw() method.");
    }
}
