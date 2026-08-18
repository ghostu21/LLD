package com.reco.lld.events;

/**
 * Demo notification sink for recommendation events.
 */
public class NotificationService implements RecoEventListener {
    @Override
    public void onEvent(RecoEvent event) {
        switch (event.getType()) {
            case RECS_GENERATED -> System.out.println("  [notify] slate ready for user "
                    + event.getUserId() + " — " + event.getPayload());
            case ITEM_CLICKED -> System.out.println("  [notify] click recorded");
            case ITEM_PURCHASED -> System.out.println("  [notify] purchase recorded — model may refresh");
            case ITEM_HIDDEN -> System.out.println("  [notify] item hidden from future slates");
        }
    }
}
