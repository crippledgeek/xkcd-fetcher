package se.disabledsecurity.xkcd.fetcher.service;

import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import javax.annotation.Nullable;

/**
 * Storage service abstraction for persisting XKCD images.
 */
public interface ImageStorageService {

    /**
     * Store image bytes under a key (e.g. "xkcd/2000").
     * @param key storage key (path/filename within the bucket)
     * @param content image bytes
     * @param contentType mime type (e.g. "image/png"); may be null to auto-detect
     * @return the key when stored
     */
    String save(String key, byte[] content, @Nullable String contentType);

    /**
     * Store image bytes under a key with auto-detected content type.
     * @param key storage key (path/filename within the bucket)
     * @param content image bytes
     * @return the key when stored
     */
    String save(String key, byte[] content);

    /**
     * Delete an image by key. No error if not exists.
     * @param key storage key
     */
    void delete(String key);

    /**
     * Check if an image exists.
     * @param key storage key
     * @return true if exists
     */
    boolean exists(String key);

    /**
     * Return a URL (presigned or direct) if available; may return the key as a fallback.
     * @param key storage key
     * @return URL or key
     */
    String url(String key);

    boolean imageExistsForComic(Comic comic);
}