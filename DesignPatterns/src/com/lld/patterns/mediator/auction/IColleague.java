package com.lld.patterns.mediator.auction;

/**
 * Colleague: a bidder talks only to {@link AuctionMediator}, never to other bidders.
 */
public interface IColleague {
    void placeBid(double amount);

    void receiveBidNotification(double bidAmount);

    String getName();
}
