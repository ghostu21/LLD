package com.gdrive.lld.access;

/**
 * Thrown by {@code FileSystemProxy} when the caller lacks the required ACL.
 */
public final class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
