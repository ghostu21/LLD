package com.gdrive.lld.storage;

import com.gdrive.lld.fs.File;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process stand-in for a local disk volume.
 */
public final class LocalStorageStrategy implements StorageStrategy {
    private final ConcurrentHashMap<String, String> blobs = new ConcurrentHashMap<>();

    @Override
    public void saveFile(File file) {
        blobs.put(file.getName(), file.readContent());
        System.out.println("Saving file locally: " + file.getName());
    }

    @Override
    public String loadFile(String fileName) {
        System.out.println("Loading file locally: " + fileName);
        return blobs.getOrDefault(fileName, "");
    }
}
