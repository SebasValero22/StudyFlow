package com.iescamp.studyflow_api.service;

/**
 * Pluggable binary storage. Implementations:
 * - LocalStorageService (default, zero-config dev/self-host)
 * - GoogleDriveStorageService (BYO Drive vault - drop-in once OAuth credentials exist;
 *   deliberately not shipped untested: OAuth against Google cannot be verified offline)
 */
public interface StorageService {

    /**
     * Stores bytes for a user and returns a opaque path/reference
     * suitable for later retrieval via {@link #resolve}.
     */
    String store(Integer userId, String originalFilename, byte[] data);

    /** Resolves a previously stored reference to readable bytes, or null if missing. */
    byte[] retrieve(String path);
}
