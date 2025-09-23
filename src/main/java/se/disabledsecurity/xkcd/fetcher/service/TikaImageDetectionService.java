package se.disabledsecurity.xkcd.fetcher.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

/**
 * Service for detecting image content types using Apache Tika.
 */
@Service
@Slf4j
public class TikaImageDetectionService implements ImageDetectionService {

    private final Tika tika;

    public TikaImageDetectionService(Tika tika) {
        this.tika = tika;
    }

    /**
     * Detects the content type of image data.
     * Uses multiple strategies: provided type, content analysis, and filename extension.
     *
     * @param content the image bytes
     * @param providedContentType the content type provided by caller (can be null)
     * @param filename the filename or key (used for extension-based detection)
     * @return the detected content type, defaulting to "application/octet-stream"
     */
    @Override
    public String detectContentType(byte[] content, @Nullable String providedContentType, @Nullable String filename) {
        try {
            // Strategy 1: Use the provided content type if valid
            if (isValidImageContentType(providedContentType)) {
                log.debug("Using provided content type '{}' for '{}'", providedContentType, filename);
                return java.util.Objects.requireNonNull(providedContentType);
            }

            // Strategy 2: Detect from content using Tika
            String detectedFromContent = tika.detect(content);
            if (isValidImageContentType(detectedFromContent)) {
                log.debug("Tika detected content type '{}' from content for '{}'", detectedFromContent, filename);
                return detectedFromContent;
            }

            // Strategy 3: Detect from filename extension
            String detectedFromFilename = tika.detect(filename);
            if (isValidImageContentType(detectedFromFilename)) {
                log.debug("Tika detected content type '{}' from filename for '{}'", detectedFromFilename, filename);
                return detectedFromFilename;
            }

            // Strategy 4: Final fallback
            log.warn("Could not determine image content type for '{}', using default", filename);
            return "application/octet-stream";

        } catch (Exception e) {
            log.warn("Error detecting content type for '{}': {}", filename, e.getMessage());
            return isValidImageContentType(providedContentType) ?
                    java.util.Objects.requireNonNullElse(providedContentType, "application/octet-stream") : "application/octet-stream";
        }
    }

    /**
     * Detects content type from content only (no filename hint).
     *
     * @param content the image bytes
     * @return the detected content type
     */
    @Override
    public String detectContentType(byte[] content) {
        return detectContentType(content, null, "unknown");
    }

    /**
     * Validates if a content type represents a valid image.
     *
     * @param contentType the content type to validate
     * @return true if it's a valid image content type
     */
    @Override
    public boolean isValidImageContentType(@Nullable String contentType) {
        return contentType != null
                && !contentType.trim().isEmpty()
                && !"application/octet-stream".equals(contentType)
                && contentType.startsWith("image/");
    }

    /**
     * Checks if the given content appears to be an image based on content analysis.
     *
     * @param content the bytes to analyze
     * @return true if the content appears to be an image
     */
    @Override
    public boolean isImage(byte[] content) {
        try {
            String contentType = tika.detect(content);
            return isValidImageContentType(contentType);
        } catch (Exception e) {
            log.warn("Error checking if content is image: {}", e.getMessage());
            return false;
        }
    }
}