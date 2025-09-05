package se.disabledsecurity.xkcd.fetcher.exception;

/**
 * Exception thrown when bucket operations fail in the storage service.
 */
public class BucketException extends RuntimeException {

    public BucketException(String message) {
        super(message);
    }

    public BucketException(String message, Throwable cause) {
        super(message, cause);
    }
}