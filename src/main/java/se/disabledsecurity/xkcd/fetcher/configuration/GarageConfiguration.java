package se.disabledsecurity.xkcd.fetcher.configuration;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.disabledsecurity.xkcd.fetcher.common.GarageProperties;

@Configuration(proxyBeanMethods = false)
public class GarageConfiguration {

    private final GarageProperties garageProperties;

    public GarageConfiguration(GarageProperties garageProperties) {
        this.garageProperties = garageProperties;
    }

    @Bean
    public MinioClient garageClient() {
        return MinioClient.builder()
                .endpoint(garageProperties.getEndpoint().toString())
                .region(garageProperties.getRegion())
                .credentials(garageProperties.getCredentials().getAccessKey(), garageProperties.getCredentials()
                        .getSecretKey())
                .build();
    }
}
