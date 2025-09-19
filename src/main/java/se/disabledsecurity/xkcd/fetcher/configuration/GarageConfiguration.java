package se.disabledsecurity.xkcd.fetcher.configuration;

import io.minio.MinioClient;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.disabledsecurity.xkcd.fetcher.common.GarageProperties;
import se.disabledsecurity.xkcd.fetcher.common.HttpClientProperties;

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
        return MinioClient.builder()
                .endpoint(garageProperties.getEndpoint().toString())
                .region(garageProperties.getRegion())
                .credentials(
                        garageProperties.getCredentials().getAccessKey(),
                        garageProperties.getCredentials().getSecretKey()
                )
                .httpClient(garageHttpClient)
                .build();
    }
}
