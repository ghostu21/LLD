package com.lld.patterns.decorator.pizza;

/**
 * Abstract decorator: IS-A BasePizza, HAS-A BasePizza (the wrappee).
 */
public abstract class ToppingDecorator implements BasePizza {
    protected final BasePizza pizza;

    public ToppingDecorator(BasePizza pizza) {
        this.pizza = pizza;
    }
}
