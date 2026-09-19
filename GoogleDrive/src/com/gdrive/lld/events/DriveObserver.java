package com.gdrive.lld.events;

/**
 * Observer of file/folder events (share, content update).
 */
public interface DriveObserver {
    void update(String message);
}
