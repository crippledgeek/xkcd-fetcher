package se.disabledsecurity.xkcd.fetcher.service;

/**
 * Service for detecting and validating image content types.
 */
public interface ImageDetectionService {

    /**
     * Detects the content type of image data.
     * Uses multiple strategies: provided type, content analysis, and filename extension.
     *
     * @param content the image bytes
     * @param providedContentType the content type provided by caller (can be null)
     * @param filename the filename or key (used for extension-based detection)
     * @return the detected content type, defaulting to "application/octet-stream"
     */
    String detectContentType(byte[] content, String providedContentType, String filename);

    /**
     * Detects content type from content only (no filename hint).
     *
     * @param content the image bytes
     * @return the detected content type
     */
    String detectContentType(byte[] content);

    /**
     * Validates if a content type represents a valid image.
     *
     * @param contentType the content type to validate
     * @return true if it's a valid image content type
     */
    boolean isValidImageContentType(String contentType);

    /**
     * Checks if the given content appears to be an image based on content analysis.
     *
     * @param content the bytes to analyze
     * @return true if the content appears to be an image
     */
    boolean isImage(byte[] content);
}