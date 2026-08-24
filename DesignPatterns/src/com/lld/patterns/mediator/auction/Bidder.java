package com.lld.patterns.mediator.auction;

/**
 * Concrete colleague: places bids and receives notifications through the mediator only.
 */
public class Bidder implements IColleague {
    protected final String name;
    protected final AuctionMediator mediator;

    public Bidder(String name, AuctionMediator mediator) {
        this.name = name;
        this.mediator = mediator;
        mediator.registerBidder(this);
    }

    @Override
    public void placeBid(double amount) {
        System.out.println("\n===> [Placing Bid] " + name + " is attempting to bid $" + amount);
        mediator.placeBid(this, amount);
    }

    @Override
    public void receiveBidNotification(double bidAmount) {
        System.out.println("[+] Bidder " + name
                + " has received a new bid notification of: " + bidAmount);
    }

    @Override
    public String getName() {
        return name;
    }
}
