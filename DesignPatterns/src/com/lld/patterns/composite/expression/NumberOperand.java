package com.lld.patterns.composite.expression;

/** Leaf: a numeric operand. Named {@code Number} in the LLD note. */
public class NumberOperand implements ArithmeticExpression {
    private final int value;

    public NumberOperand(int value) {
        this.value = value;
    }

    @Override
    public int evaluate() {
        System.out.println("Number value is: " + value);
        return value;
    }
}
