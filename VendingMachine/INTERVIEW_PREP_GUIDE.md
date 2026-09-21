# Vending Machine LLD — Interview Prep Guide

**One-liner:**  
> “Cents with AtomicInteger, Strategy payments, Command collect/refund, slot StockState (Available/Low/Out + expiry), machine Idle/Collecting/Maintenance, and an event bus so the display is just one subscriber.”

## Patterns

| Pattern | Where |
|---------|-------|
| Strategy | Coin / card / bill / mobile |
| Factory | Snack vs drink |
| State | Machine + stock |
| Command | Collect / refund |
| Observer | Event bus |
| Facade | Hardware ports |

## Traps

| Trap | Better |
|------|--------|
| `float` money | Integer cents |
| `synchronized` only on machine, racy balance | Atomic cents anyway |
| `if (qty==0)` | Stock state + expiry |
| `display.update` inside vend | Publish event |
| Restock from customer keypad | Admin PIN / cabinet switch |
