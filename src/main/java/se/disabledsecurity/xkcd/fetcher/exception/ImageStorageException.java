package se.disabledsecurity.xkcd.fetcher.exception;

/**
 * Exception thrown when image storage operations fail.
 */
public class ImageStorageException extends RuntimeException {

    public ImageStorageException(String message) {
        super(message);
    }

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}