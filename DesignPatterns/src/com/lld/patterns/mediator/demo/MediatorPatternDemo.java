package com.lld.patterns.mediator.demo;

import com.lld.patterns.mediator.auction.AuctionHouse;
import com.lld.patterns.mediator.auction.AuctionMediator;
import com.lld.patterns.mediator.auction.Bidder;
import com.lld.patterns.mediator.auction.IColleague;

public class MediatorPatternDemo {
    public static void main(String[] args) {
        System.out.println("\n###### Mediator Design Pattern ######");
        System.out.println("\n===> Welcome to the Auction House!\n");

        AuctionMediator auctionHouse = new AuctionHouse("Vintage Guitar", 100.0);

        IColleague alice = new Bidder("Alice", auctionHouse);
        IColleague bob = new Bidder("Bob", auctionHouse);
        IColleague charlie = new Bidder("Charlie", auctionHouse);

        alice.placeBid(150.0);
        bob.placeBid(250.0);
        charlie.placeBid(300.0);
        alice.placeBid(300.0);
        bob.placeBid(900.0);

        auctionHouse.closeAuction();
    }
}
