package com.gdrive.lld.storage;

import com.gdrive.lld.fs.File;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process stand-in for GCS / S3. Swap via {@code FileSystemManager.setStorageStrategy}.
 */
public final class CloudStorageStrategy implements StorageStrategy {
    private final ConcurrentHashMap<String, String> blobs = new ConcurrentHashMap<>();

    @Override
    public void saveFile(File file) {
        blobs.put(file.getName(), file.readContent());
        System.out.println("Saving file to cloud: " + file.getName());
    }

    @Override
    public String loadFile(String fileName) {
        System.out.println("Loading file from cloud: " + fileName);
        return blobs.getOrDefault(fileName, "");
    }
}
