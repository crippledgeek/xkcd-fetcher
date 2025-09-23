package se.disabledsecurity.xkcd.fetcher.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Generic HTTP client configuration properties.
 * Works for Apache HttpClient, JDK HttpClient, or OkHttp.
 */
@Data
@ConfigurationProperties(prefix = "httpclient")
public class HttpClientProperties {

    /** Pooling. */
    private int maxTotal = 100;
    private int defaultMaxPerRoute = 20;

    /** Redirects. */
    private boolean redirectsEnabled = true;
    private boolean circularRedirectsAllowed = false;
    private int maxRedirects = 20;

    /** Timeouts. */
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration connectionRequestTimeout = Duration.ofSeconds(5);
    private Duration responseTimeout = Duration.ofSeconds(30);
}
