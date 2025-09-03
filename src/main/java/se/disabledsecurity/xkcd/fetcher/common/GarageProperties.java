package se.disabledsecurity.xkcd.fetcher.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;

@Data
@ConfigurationProperties(prefix = "garage")
public class GarageProperties {

    private URL endpoint;
    private String region;
    private Credentials credentials = new Credentials();
    private String bucket;

    @Data
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }
}
