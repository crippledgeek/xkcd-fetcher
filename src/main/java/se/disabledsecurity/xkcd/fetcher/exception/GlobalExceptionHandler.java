package se.disabledsecurity.xkcd.fetcher.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Handles all exceptions and returns a ProblemDetail with a generic error message.
     *
     * @param ex      the exception that was thrown
     * @param request the current HTTP request
     * @return a ProblemDetail containing the error information
     */

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGenericException(Exception ex, ServerHttpRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred while processing the request"
        );

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(request.getURI());
        problemDetail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");
        problemDetail.setProperty("errorMessage", ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(NoSuchComicFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNoSuchComicFoundException(NoSuchComicFoundException ex, ServerHttpRequest request) {
        log.error("No such comic found: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "No such comic found");

        problemDetail.setTitle("Not Found");
        problemDetail.setInstance(request.getURI());
        problemDetail.setProperty("errorCode", "NOT_FOUND");
        problemDetail.setProperty("errorMessage", ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}