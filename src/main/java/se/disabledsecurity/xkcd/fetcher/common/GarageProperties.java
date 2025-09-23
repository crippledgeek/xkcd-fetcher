package se.disabledsecurity.xkcd.fetcher.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import javax.annotation.Nullable;

@Data
@ConfigurationProperties(prefix = "garage")
public class GarageProperties {

    @Nullable
    private URL endpoint;
    @Nullable
    private String region;
    @Nullable
    private Credentials credentials = new Credentials();
    @Nullable
    private String bucket;

    @Data
    public static class Credentials {
        @Nullable
        private String accessKey;
        @Nullable
        private String secretKey;
    }
}
