package com.amazon.lld.returns;

/**
 * Workflow state for a return request.
 */
public enum ReturnStatus {
    REQUESTED,
    APPROVED,
    REJECTED,
    ITEM_RECEIVED,
    REFUNDED
}
