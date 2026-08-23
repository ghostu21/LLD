package com.lld.patterns.composite.expression;

/**
 * Composite: left and right are themselves expressions (number or nested op).
 * Tree for {@code 2 * (1 + 7)} is multiply(2, add(1, 7)).
 */
public class Expression implements ArithmeticExpression {
    private final ArithmeticExpression leftExpression;
    private final ArithmeticExpression rightExpression;
    private final OperationType operation;

    public Expression(ArithmeticExpression leftPart, ArithmeticExpression rightPart, OperationType operation) {
        this.leftExpression = leftPart;
        this.rightExpression = rightPart;
        this.operation = operation;
    }

    @Override
    public int evaluate() {
        int value = 0;
        switch (operation) {
            case ADD:
                value = leftExpression.evaluate() + rightExpression.evaluate();
                break;
            case SUBTRACT:
                value = leftExpression.evaluate() - rightExpression.evaluate();
                break;
            case DIVIDE:
                value = leftExpression.evaluate() / rightExpression.evaluate();
                break;
            case MULTIPLY:
                value = leftExpression.evaluate() * rightExpression.evaluate();
                break;
        }
        System.out.println("Expression value is:" + value);
        return value;
    }
}
