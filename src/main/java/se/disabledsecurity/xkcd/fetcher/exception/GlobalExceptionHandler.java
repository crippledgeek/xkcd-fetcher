package se.disabledsecurity.xkcd.fetcher.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
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
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred while processing the request"
        );

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");
        problemDetail.setProperty("errorMessage", ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(NoSuchComicFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNoSuchComicFoundException(NoSuchComicFoundException ex, HttpServletRequest request) {
        log.error("No such comic found: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "No such comic found");

        problemDetail.setTitle("Not Found");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("errorCode", "NOT_FOUND");
        problemDetail.setProperty("errorMessage", ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(SchedulerCleanupException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleSchedulerCleanupException(SchedulerCleanupException ex, HttpServletRequest request) {
        log.error("Scheduler cleanup failed: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Scheduler cleanup operation failed"
        );

        problemDetail.setTitle("Scheduler Cleanup Error");
        problemDetail.setProperty("errorCode", "SCHEDULER_CLEANUP_FAILED");
        problemDetail.setProperty("errorMessage", ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ImageStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleImageStorageException(ImageStorageException ex, HttpServletRequest request) {
        log.error("Image storage error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to perform image storage operation"
        );

        problemDetail.setTitle("Image Storage Error");
        problemDetail.setProperty("errorCode", "IMAGE_STORAGE_ERROR");
        problemDetail.setProperty("errorMessage", ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
    @ExceptionHandler(BucketException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleBucketException(BucketException ex, HttpServletRequest request) {
        log.error("Bucket operation failed: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Storage bucket operation failed"
        );

        problemDetail.setTitle("Bucket Error");
        problemDetail.setProperty("errorCode", "BUCKET_ERROR");
        problemDetail.setProperty("errorMessage", ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}