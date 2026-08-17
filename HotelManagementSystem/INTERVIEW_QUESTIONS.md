# Hotel Management — Interview Questions

1. Why is room availability date-based instead of a single status flag?
2. Walk through booking under concurrency — what do you lock?
3. Optimistic vs pessimistic vs Redis distributed lock — when each?
4. How do you implement “full refund if cancelled 24h before check-in”?
5. Why publish notifications on an event bus instead of calling EmailService directly?
6. Model housekeeping as a workflow — what states and who owns the transition?
7. How do service charges (food, amenities) stay consistent with the final bill?
8. How would you query “which guest booked room 201 last weekend?” efficiently?
9. What breaks if two app servers book without a distributed lock?
10. How would you extend to multi-hotel inventory and overbooking?
