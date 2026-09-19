package com.gdrive.lld.access;

/**
 * Drive-style ACL. Lower ordinal = more power so
 * {@code userLevel.ordinal() <= required.ordinal()} means allowed.
 */
public enum AccessLevel {
    OWNER,
    EDITOR,
    COMMENTER,
    VIEWER,
    NONE;

    public boolean allows(AccessLevel required) {
        return ordinal() <= required.ordinal();
    }
}
