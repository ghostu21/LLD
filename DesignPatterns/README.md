# Design Patterns

GoF catalog implemented as **one Java package per pattern**, with a README that covers when to use it, SOLID, trade-offs, and interview Q&A.

| Pattern | Type | Package | Notes |
|---------|------|---------|--------|
| [Strategy](src/com/lld/patterns/strategy/doc/README.md) | Behavioral | `com.lld.patterns.strategy` | Swap an algorithm at runtime (drive mode, payment) |
| [Observer](src/com/lld/patterns/observer/doc/README.md) | Behavioral | `com.lld.patterns.observer` | Notify subscribers of state (weather, Notify Me) |

## Run

```bash
cd DesignPatterns
javac -d out $(find src -name '*.java')
java -cp out com.lld.patterns.strategy.demo.StrategyPatternDemo
java -cp out com.lld.patterns.observer.demo.ObserverPatternDemo
```
