# Design Patterns

GoF catalog implemented as **one Java package per pattern**, with a README that covers when to use it, SOLID, trade-offs, and interview Q&A.

| Pattern | Type | Package | Notes |
|---------|------|---------|--------|
| [Strategy](src/com/lld/patterns/strategy/doc/README.md) | Behavioral | `com.lld.patterns.strategy` | Swap an algorithm at runtime (drive mode, payment) |
| [Observer](src/com/lld/patterns/observer/doc/README.md) | Behavioral | `com.lld.patterns.observer` | Notify subscribers of state (weather, Notify Me) |
| [Chain of Responsibility](src/com/lld/patterns/chainofresponsibility/doc/README.md) | Behavioral | `com.lld.patterns.chainofresponsibility` | Pass a request along handlers (logging, ATM) |
| [Command](src/com/lld/patterns/command/doc/README.md) | Behavioral | `com.lld.patterns.command` | Request as object + undo (AC remote) |
| [Template Method](src/com/lld/patterns/templatemethod/doc/README.md) | Behavioral | `com.lld.patterns.templatemethod` | Algorithm skeleton; subclasses fill steps (payments) |
| [State](src/com/lld/patterns/state/doc/README.md) | Behavioral | `com.lld.patterns.state` | Behavior follows mode (traffic light, vending machine) |
| [Decorator](src/com/lld/patterns/decorator/doc/README.md) | Structural | `com.lld.patterns.decorator` | Wrap to add behavior (pizza toppings) |
| [Facade](src/com/lld/patterns/facade/doc/README.md) | Structural | `com.lld.patterns.facade` | Simple API over subsystems (place order) |
| [Proxy](src/com/lld/patterns/proxy/doc/README.md) | Structural | `com.lld.patterns.proxy` | Stand-in that controls access (employee DAO) |
| [Adapter](src/com/lld/patterns/adapter/doc/README.md) | Structural | `com.lld.patterns.adapter` | Incompatible APIs (pounds → kg scale) |
| [Composite](src/com/lld/patterns/composite/doc/README.md) | Structural | `com.lld.patterns.composite` | Tree of parts (files, `2*(1+7)`) |

## Run

```bash
cd DesignPatterns
javac -d out $(find src -name '*.java')
java -cp out com.lld.patterns.strategy.demo.StrategyPatternDemo
java -cp out com.lld.patterns.observer.demo.ObserverPatternDemo
java -cp out com.lld.patterns.chainofresponsibility.demo.ChainOfResponsibilityDemo
java -cp out com.lld.patterns.command.demo.CommandPatternDemo
java -cp out com.lld.patterns.templatemethod.demo.TemplateMethodDemo
java -cp out com.lld.patterns.state.demo.StatePatternDemo
java -cp out com.lld.patterns.decorator.demo.DecoratorPatternDemo
java -cp out com.lld.patterns.facade.demo.FacadePatternDemo
java -cp out com.lld.patterns.proxy.demo.ProxyPatternDemo
java -cp out com.lld.patterns.adapter.demo.AdapterPatternDemo
java -cp out com.lld.patterns.composite.demo.CompositePatternDemo
```
