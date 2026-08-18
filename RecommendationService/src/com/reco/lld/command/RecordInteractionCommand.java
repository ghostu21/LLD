package com.reco.lld.command;

import com.reco.lld.account.User;
import com.reco.lld.profile.InteractionType;
import com.reco.lld.service.InteractionService;

/**
 * Encapsulates "record click / purchase / hide" as an executable command.
 */
public class RecordInteractionCommand implements Command {
    private final InteractionService service;
    private final User actor;
    private final String itemId;
    private final InteractionType type;

    public RecordInteractionCommand(InteractionService service, User actor,
                                    String itemId, InteractionType type) {
        this.service = service;
        this.actor = actor;
        this.itemId = itemId;
        this.type = type;
    }

    @Override
    public void execute() {
        service.record(actor, itemId, type);
    }
}
