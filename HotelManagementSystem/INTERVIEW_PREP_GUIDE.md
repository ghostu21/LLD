# Hotel Management — Interview Prep Guide

Use with [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md) and demos in `README.md`.

## 15-minute whiteboard script

1. **Requirements** — rooms, search/book, cancel/refund, notifications, HK, payments (2 min)
2. **Core entities** — Hotel, Room + calendar, RoomBooking, Guest, Bill, HouseKeepingTask (3 min)
3. **Booking flow** — Search → Lock → Validate → Create → Mark reserved (3 min)
4. **Pitfalls** — calendar vs status, async notify, refund policy, HK workflow, locks (5 min)
5. **Scale** — Redis lock TTL, Kafka reminders, shard by hotelId (2 min)

## Run before the interview

```bash
cd HotelManagementSystem
javac -d out $(find src -name '*.java')
java -cp out com.hotel.lld.demo.HotelManagementService overlap
java -cp out com.hotel.lld.demo.HotelManagementService cancel
java -cp out com.hotel.lld.demo.HotelManagementService concurrent
```

## Sound bites

- “Availability is date-based, not status-based.”
- “Refunds are business rules, not controller logic.”
- “Housekeeping is a workflow, not a method.”
- “Booking systems fail when locks are missing.”
