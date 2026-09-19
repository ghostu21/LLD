package com.gdrive.lld.storage;

import com.gdrive.lld.fs.File;

/**
 * Strategy for persisting blob bytes (local disk vs cloud object store).
 */
public interface StorageStrategy {
    void saveFile(File file);

    String loadFile(String fileName);
}
