package com.lld.patterns.mediator.auction;

/**
 * Mediator: the only object that knows all bidders and the bidding rules.
 */
public interface AuctionMediator {
    void registerBidder(IColleague bidder);

    void placeBid(IColleague bidder, double bidAmount);

    void closeAuction();
}
