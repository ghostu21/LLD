# Vending Machine LLD — Interview Questions (5+ YOE)

1. Why is `float` wrong for money even on a single thread?
2. Two coins arrive on GPIO at once — where is the lock vs the atomic?
3. Debit succeeded, motor jammed — how do Commands help?
4. Low-stock alert vs out-of-stock — who consumes the event?
5. Expiry: treat as OOS or a fourth state? Trade-offs.
6. CAS on quantity vs synchronized slot — last-item race.
7. Maintenance: why a State instead of `if (admin)` flags?
8. Card authorization vs cash in the box — when is RefundCommand a no-op?
9. How would you vend two items in one session without exploding the state machine?
10. Fleet: what belongs on-device vs in the cloud price service?
