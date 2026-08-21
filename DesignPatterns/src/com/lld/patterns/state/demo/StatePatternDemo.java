package com.lld.patterns.state.demo;

import com.lld.patterns.state.traffic.TrafficLight;
import com.lld.patterns.state.vending.Coin;
import com.lld.patterns.state.vending.Item;
import com.lld.patterns.state.vending.ItemShelf;
import com.lld.patterns.state.vending.ItemType;
import com.lld.patterns.state.vending.State;
import com.lld.patterns.state.vending.VendingMachine;

public class StatePatternDemo {
    public static void main(String[] args) {
        runTrafficLight();
        runVendingMachine();
    }

    private static void runTrafficLight() {
        System.out.println("###### State Design Pattern ######");
        System.out.println("###### Example: Traffic Light ######");
        TrafficLight trafficLight = new TrafficLight();
        trafficLight.change();
        trafficLight.change();
        trafficLight.change();
        System.out.println();
    }

    private static void runVendingMachine() {
        System.out.println("###### Example: Vending Machine ######");
        VendingMachine vendingMachine = new VendingMachine();
        try {
            System.out.println("|");
            System.out.println("filling up the inventory");
            System.out.println("|");
            fillUpInventory(vendingMachine);
            displayInventory(vendingMachine);

            System.out.println("|");
            System.out.println("clicking on InsertCoinButton");
            System.out.println("|");

            State vendingState = vendingMachine.getVendingMachineState();
            vendingState.clickOnInsertCoinButton(vendingMachine);

            vendingState = vendingMachine.getVendingMachineState();
            vendingState.insertCoin(vendingMachine, Coin.NICKEL);
            vendingState.insertCoin(vendingMachine, Coin.QUARTER);

            System.out.println("|");
            System.out.println("clicking on ProductSelectionButton");
            System.out.println("|");
            vendingState.clickOnStartProductSelectionButton(vendingMachine);

            vendingState = vendingMachine.getVendingMachineState();
            vendingState.chooseProduct(vendingMachine, 102);

            displayInventory(vendingMachine);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            displayInventory(vendingMachine);
        }
    }

    private static void fillUpInventory(VendingMachine vendingMachine) {
        ItemShelf[] slots = vendingMachine.getInventory().getInventory();
        for (int i = 0; i < slots.length; i++) {
            Item newItem = new Item();
            if (i < 3) {
                newItem.setType(ItemType.COKE);
                newItem.setPrice(12);
            } else if (i < 5) {
                newItem.setType(ItemType.PEPSI);
                newItem.setPrice(9);
            } else if (i < 7) {
                newItem.setType(ItemType.JUICE);
                newItem.setPrice(13);
            } else {
                newItem.setType(ItemType.SODA);
                newItem.setPrice(7);
            }
            slots[i].setItem(newItem);
            slots[i].setSoldOut(false);
        }
    }

    private static void displayInventory(VendingMachine vendingMachine) {
        ItemShelf[] slots = vendingMachine.getInventory().getInventory();
        for (ItemShelf slot : slots) {
            System.out.println("CodeNumber: " + slot.getCode()
                    + " Item: " + slot.getItem().getType().name()
                    + " Price: " + slot.getItem().getPrice()
                    + " isAvailable: " + !slot.isSoldOut());
        }
    }
}
