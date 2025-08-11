package se.disabledsecurity.xkcd.fetcher.exception;

public class NoSuchComicFoundException extends RuntimeException {

    public NoSuchComicFoundException(String message) {
        super(message);
    }
}
