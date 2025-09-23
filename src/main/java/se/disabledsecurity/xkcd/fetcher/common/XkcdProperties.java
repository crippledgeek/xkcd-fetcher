package se.disabledsecurity.xkcd.fetcher.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Configuration properties for interacting with the XKCD API.
 * These properties help configure the base URL for retrieving comics,
 * image resources, and scheduling settings.
 * <p>
 * It is expected that this class will be bound to configuration
 * properties with the prefix "xkcd".
 * <p>
 * Example configuration keys:
 * - xkcd.comic-base-url
 * - xkcd.image-base-url
 * - xkcd.excluded-comic-numbers
 * - xkcd.scheduler.initial-delay-in-minutes
 * - xkcd.scheduler.interval-in-minutes
 * <p>
 * The settings provided here are used by various components such as
 * clients for API interaction and scheduled jobs.
 */
@Data
@ConfigurationProperties(prefix = "xkcd")
public class XkcdProperties {
    /**
     * The base URL for xkcd comics.
     */
    @Nullable
    private URL comicBaseUrl;

    /**
     * The base URL for retrieving xkcd comic images.
     */
    @Nullable
    private URL imageBaseUrl;

    /**
     * List of comic numbers to exclude from image backfilling.
     * These comics typically have broken image URLs or other issues
     * that prevent successful image fetching.
     */
    private Set<Integer> excludedComicNumbers = Set.of();

    /**
     * The scheduler properties for xkcd comics.
     */
    private Scheduler scheduler = new Scheduler();

    /**
     * This class is used to map the scheduler properties for xkcd comics.
     */
    @Data
    public static class Scheduler {

        /**
         * The initial delay (in minutes) before the scheduler runs for the first time.
         */
        private int initialDelayInMinutes = 1;

        /**
         * The interval (in minutes) between each run of the scheduler.
         */
        private int intervalInMinutes = 1440;
    }
}