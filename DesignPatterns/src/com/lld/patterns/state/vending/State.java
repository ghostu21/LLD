package com.lld.patterns.state.vending;

import java.util.List;

/**
 * State interface as an abstract class: illegal operations throw.
 * Concrete states override only what is valid.
 */
public abstract class State {
    protected Exception notAllowed() {
        return new Exception("Operation not allowed in " + getClass().getSimpleName());
    }

    public void clickOnInsertCoinButton(VendingMachine machine) throws Exception {
        throw notAllowed();
    }

    public void clickOnStartProductSelectionButton(VendingMachine machine) throws Exception {
        throw notAllowed();
    }

    public void insertCoin(VendingMachine machine, Coin coin) throws Exception {
        throw notAllowed();
    }

    public void chooseProduct(VendingMachine machine, int codeNumber) throws Exception {
        throw notAllowed();
    }

    public int getChange(int returnChangeMoney) throws Exception {
        throw notAllowed();
    }

    public Item dispenseProduct(VendingMachine machine, int codeNumber) throws Exception {
        throw notAllowed();
    }

    public List<Coin> refundFullMoney(VendingMachine machine) throws Exception {
        throw notAllowed();
    }

    public void updateInventory(VendingMachine machine, Item item, int codeNumber) throws Exception {
        throw notAllowed();
    }
}
