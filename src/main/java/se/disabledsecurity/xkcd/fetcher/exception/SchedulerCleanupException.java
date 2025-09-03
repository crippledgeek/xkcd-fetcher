package se.disabledsecurity.xkcd.fetcher.exception;

public class SchedulerCleanupException extends RuntimeException {
    public SchedulerCleanupException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulerCleanupException(String message) {
        super(message);
    }
}