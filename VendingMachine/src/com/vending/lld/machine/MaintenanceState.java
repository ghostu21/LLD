package com.vending.lld.machine;

import com.vending.lld.events.VendingEvent;
import com.vending.lld.events.VendingEventType;
final class MaintenanceState extends IllegalOpsState {
    @Override
    public void exitMaintenance(VendingMachine machine) {
        machine.setState(new IdleState());
        machine.publish(new VendingEvent(VendingEventType.MAINTENANCE, "Left maintenance"));
    }

    @Override
    public void enterMaintenance(VendingMachine machine, String pin) {
        machine.publish(new VendingEvent(VendingEventType.MAINTENANCE, "Already in maintenance"));
    }

    @Override
    public String name() {
        return "MAINTENANCE";
    }
}
