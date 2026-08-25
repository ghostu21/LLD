package com.lld.patterns.factory.shape;

public class Square implements Shape {
    @Override
    public void computeArea() {
        System.out.println("Inside Square::computeArea() method.");
    }

    @Override
    public void draw() {
        System.out.println("Inside Square::draw() method.");
    }
}
