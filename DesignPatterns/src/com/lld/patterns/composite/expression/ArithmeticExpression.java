package com.lld.patterns.composite.expression;

/** Component: a number or an operator node both evaluate to an int. */
public interface ArithmeticExpression {
    int evaluate();
}
