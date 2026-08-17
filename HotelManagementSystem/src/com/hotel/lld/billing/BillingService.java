package com.hotel.lld.billing;

import com.hotel.lld.booking.Refund;
import com.hotel.lld.booking.RoomBooking;
import com.hotel.lld.room.Room;
import com.hotel.lld.service.ChargeType;
import com.hotel.lld.service.ServiceCharge;

/**
 * Builds and mutates itemized bills for room stays and service charges.
 */
public class BillingService {

    public Bill generateBill(RoomBooking booking, Room room) {
        Bill bill = new Bill();
        int nights = booking.getDurationInDays();
        double roomCharge = room.getBookingPrice() * nights;
        bill.addItem(new BillItem(BillItemType.ROOM_CHARGE,
                "Room " + room.getRoomNumber() + " (" + nights + " night(s))", roomCharge));
        booking.setBill(bill);
        return bill;
    }

    public void appendServiceCharge(Bill bill, ServiceCharge charge) {
        if (bill == null) {
            throw new IllegalArgumentException("bill is required");
        }
        bill.addItem(new BillItem(mapType(charge.getType()), charge.getDescription(), charge.getAmount()));
    }

    public void refreshWithCharges(RoomBooking booking) {
        // charges already appended when added; hook for checkout-time reconciliation
        if (booking.getBill() == null) {
            return;
        }
    }

    public void applyRefund(Bill bill, Refund refund) {
        if (bill == null || refund.getAmount() <= 0) {
            return;
        }
        bill.addItem(new BillItem(BillItemType.REFUND,
                "Refund: " + refund.getReason(), -refund.getAmount()));
    }

    private BillItemType mapType(ChargeType type) {
        return switch (type) {
            case FOOD -> BillItemType.FOOD;
            case ROOM_SERVICE -> BillItemType.ROOM_SERVICE;
            case AMENITY -> BillItemType.AMENITY;
            case HOUSEKEEPING -> BillItemType.HOUSEKEEPING;
            case OTHER -> BillItemType.OTHER;
        };
    }
}
