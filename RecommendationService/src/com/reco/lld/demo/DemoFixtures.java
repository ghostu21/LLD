package com.reco.lld.demo;

import com.reco.lld.account.AuthService;
import com.reco.lld.account.Session;
import com.reco.lld.account.User;
import com.reco.lld.account.UserRole;
import com.reco.lld.account.UserStore;
import com.reco.lld.cache.TtlCache;
import com.reco.lld.catalog.Catalog;
import com.reco.lld.catalog.Category;
import com.reco.lld.catalog.Item;
import com.reco.lld.catalog.ItemStatus;
import com.reco.lld.events.AsyncEventBus;
import com.reco.lld.events.NotificationService;
import com.reco.lld.events.RecoEventType;
import com.reco.lld.experiment.ExperimentAssigner;
import com.reco.lld.pipeline.FilterChain;
import com.reco.lld.profile.InteractionStore;
import com.reco.lld.profile.InteractionType;
import com.reco.lld.profile.ProfileService;
import com.reco.lld.ranking.RankingStrategyFactory;
import com.reco.lld.request.RecommendationResponse;
import com.reco.lld.security.RateLimiter;
import com.reco.lld.service.CatalogAdmin;
import com.reco.lld.service.InteractionService;
import com.reco.lld.service.RecommendationFacade;

import java.time.Duration;
import java.util.Set;

/**
 * Shared users, catalog, history, and wired services for all demos.
 */
public final class DemoFixtures {
    public final AuthService auth;
    public final User alice;
    public final User bob;
    public final User charlie;
    public final User admin;
    public final User guest;
    public final Session aliceSession;
    public final Session guestSession;
    public final Session adminSession;

    public final Item phone;
    public final Item headphones;
    public final Item charger;
    public final Item cleanCode;
    public final Item dddBook;
    public final Item tshirt;
    public final Item sneakers;
    public final Item lamp;
    public final Item bannedGag;
    public final Item outOfStock;

    public final Catalog catalog;
    public final RecommendationFacade reco;
    public final InteractionService interactions;
    public final CatalogAdmin catalogAdmin;
    public final AsyncEventBus eventBus;
    public final RateLimiter rateLimiter;

    public DemoFixtures() throws Exception {
        UserStore users = new UserStore();
        auth = new AuthService(users);
        alice = auth.register("alice", "secret123", UserRole.MEMBER, "alice@example.com");
        bob = auth.register("bob", "secret123", UserRole.MEMBER, "bob@example.com");
        charlie = auth.register("charlie", "secret123", UserRole.MEMBER, "charlie@example.com");
        admin = auth.register("admin", "secret123", UserRole.ADMIN, "admin@example.com");
        aliceSession = auth.login("alice", "secret123");
        adminSession = auth.login("admin", "secret123");
        guestSession = auth.guestSession();
        guest = auth.requireUser(guestSession.getToken());

        catalog = new Catalog();
        phone = add("Smartphone X", Category.ELECTRONICS, Set.of("mobile", "gadget"), 699);
        headphones = add("Wireless Headphones", Category.ELECTRONICS, Set.of("audio", "gadget"), 149);
        charger = add("Fast Charger", Category.ELECTRONICS, Set.of("mobile", "power"), 29);
        cleanCode = add("Clean Code", Category.BOOKS, Set.of("software", "craft"), 39);
        dddBook = add("Domain-Driven Design", Category.BOOKS, Set.of("software", "architecture"), 49);
        tshirt = add("Cotton T-Shirt", Category.CLOTHING, Set.of("casual"), 19);
        sneakers = add("Running Shoes", Category.SPORTS, Set.of("fitness"), 89);
        lamp = add("Desk Lamp", Category.HOME, Set.of("office"), 45);
        bannedGag = add("Banned Gadget", Category.ELECTRONICS, Set.of("gadget"), 1);
        outOfStock = add("Rare Vinyl", Category.HOME, Set.of("music"), 99);
        bannedGag.setStatus(ItemStatus.BANNED);
        outOfStock.setStatus(ItemStatus.OUT_OF_STOCK);

        eventBus = new AsyncEventBus();
        NotificationService notifications = new NotificationService();
        for (RecoEventType type : RecoEventType.values()) {
            eventBus.subscribe(type, notifications);
        }

        TtlCache<RecommendationResponse> cache = new TtlCache<>(Duration.ofSeconds(60));
        InteractionStore store = new InteractionStore();
        interactions = new InteractionService(catalog, store, cache, eventBus);
        rateLimiter = new RateLimiter(80, 10_000);
        reco = new RecommendationFacade(
                catalog,
                new ProfileService(store, catalog),
                interactions,
                new RankingStrategyFactory(),
                FilterChain.defaultChain(),
                new ExperimentAssigner(),
                rateLimiter,
                cache,
                eventBus);
        catalogAdmin = new CatalogAdmin(catalog, cache);

        seedHistory();
    }

    private Item add(String title, Category category, Set<String> tags, double price) {
        Item item = new Item(title, category, tags, price);
        catalog.add(item);
        return item;
    }

    /**
     * Alice and Charlie share electronics purchases so collaborative filtering
     * can surface headphones to Alice. Bob is a books reader (content-based).
     * Popularity is boosted by extra views on Clean Code.
     */
    private void seedHistory() {
        interactions.seed(alice.getUserId(), phone.getItemId(), InteractionType.PURCHASE);
        interactions.seed(alice.getUserId(), charger.getItemId(), InteractionType.CLICK);
        interactions.seed(charlie.getUserId(), phone.getItemId(), InteractionType.PURCHASE);
        interactions.seed(charlie.getUserId(), headphones.getItemId(), InteractionType.PURCHASE);
        interactions.seed(bob.getUserId(), cleanCode.getItemId(), InteractionType.PURCHASE);
        interactions.seed(bob.getUserId(), dddBook.getItemId(), InteractionType.CLICK);
        interactions.seed(alice.getUserId(), cleanCode.getItemId(), InteractionType.VIEW);
        interactions.seed(charlie.getUserId(), cleanCode.getItemId(), InteractionType.VIEW);
        interactions.seed(bob.getUserId(), sneakers.getItemId(), InteractionType.VIEW);
    }
}
