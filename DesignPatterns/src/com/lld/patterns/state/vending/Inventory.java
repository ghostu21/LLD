package com.lld.patterns.state.vending;

public class Inventory {
    private ItemShelf[] inventory;

    public Inventory(int itemCount) {
        inventory = new ItemShelf[itemCount];
        initialEmptyInventory();
    }

    public ItemShelf[] getInventory() {
        return inventory;
    }

    public void initialEmptyInventory() {
        int startCode = 101;
        for (int i = 0; i < inventory.length; i++) {
            ItemShelf space = new ItemShelf();
            space.setCode(startCode);
            space.setSoldOut(true);
            inventory[i] = space;
            startCode++;
        }
    }

    public void addItem(Item item, int codeNumber) throws Exception {
        for (ItemShelf itemShelf : inventory) {
            if (itemShelf.getCode() == codeNumber) {
                if (itemShelf.isSoldOut()) {
                    itemShelf.setItem(item);
                    itemShelf.setSoldOut(false);
                    return;
                }
                throw new Exception("already item is present, you can not add item here");
            }
        }
        throw new Exception("Invalid Code");
    }

    public Item getItem(int codeNumber) throws Exception {
        for (ItemShelf itemShelf : inventory) {
            if (itemShelf.getCode() == codeNumber) {
                if (itemShelf.isSoldOut()) {
                    throw new Exception("item already sold out");
                }
                return itemShelf.getItem();
            }
        }
        throw new Exception("Invalid Code");
    }

    public void updateSoldOutItem(int codeNumber) {
        for (ItemShelf itemShelf : inventory) {
            if (itemShelf.getCode() == codeNumber) {
                itemShelf.setSoldOut(true);
            }
        }
    }
}
