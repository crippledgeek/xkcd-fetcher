package se.disabledsecurity.xkcd.fetcher.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;

/**
 * This class is used to map the properties for xkcd comics.
 * It contains the base URL and scheduler properties.
 */
@Data
@ConfigurationProperties(prefix = "xkcd")
public class XkcdProperties {
    /**
     * The base URL for xkcd comics.
     */
    private URL baseUrl;
    /**
     * The scheduler properties for xkcd comics.
     */
    private Scheduler scheduler;

    /**
     * This class is used to map the scheduler properties for xkcd comics.
     */
    @Data
    public static class Scheduler {

        /**
         * The initial delay (in minutes) before the scheduler runs for the first time.
         */
        private int initialDelayInMinutes;
        /**
         * The interval (in minutes) between each run of the scheduler.
         */
        private int intervalInMinutes;
    }

}
