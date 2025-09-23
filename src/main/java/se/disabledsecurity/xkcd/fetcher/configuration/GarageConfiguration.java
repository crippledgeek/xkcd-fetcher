package se.disabledsecurity.xkcd.fetcher.configuration;

import io.minio.MinioClient;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.disabledsecurity.xkcd.fetcher.common.GarageProperties;
import se.disabledsecurity.xkcd.fetcher.common.HttpClientProperties;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({GarageProperties.class, HttpClientProperties.class})
public class GarageConfiguration {

    private final GarageProperties garageProperties;
    private final HttpClientProperties httpClientProperties;

    public GarageConfiguration(GarageProperties garageProperties, HttpClientProperties httpClientProperties) {
        this.garageProperties = garageProperties;
        this.httpClientProperties = httpClientProperties;
    }

    @Bean
    public ConnectionPool garageConnectionPool() {
        return new ConnectionPool(
                httpClientProperties.getMaxTotal(),
                httpClientProperties.getResponseTimeout().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Bean
    public OkHttpClient garageHttpClient(ConnectionPool garageConnectionPool) {
        return new OkHttpClient.Builder()
                .connectionPool(garageConnectionPool)
                .connectTimeout(httpClientProperties.getConnectTimeout())
                .readTimeout(httpClientProperties.getResponseTimeout())
                .writeTimeout(httpClientProperties.getResponseTimeout())
                .build();
    }

    @Bean
    public MinioClient garageClient(OkHttpClient garageHttpClient) {
        var creds = Objects.requireNonNull(garageProperties.getCredentials(), "garage.credentials must be set");
        var endpoint = Objects.requireNonNull(garageProperties.getEndpoint(), "garage.endpoint must be set");
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(endpoint.toString())
                .credentials(Objects.requireNonNull(creds.getAccessKey(), "garage.credentials.accessKey must be set"),
                             Objects.requireNonNull(creds.getSecretKey(), "garage.credentials.secretKey must be set")
                )
                .httpClient(garageHttpClient);
        if (garageProperties.getRegion() != null && !garageProperties.getRegion().isBlank()) {
            builder.region(garageProperties.getRegion());
        }
        return builder.build();
    }
}
