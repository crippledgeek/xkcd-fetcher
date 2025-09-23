package se.disabledsecurity.xkcd.fetcher.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;
import se.disabledsecurity.xkcd.fetcher.service.XKCDImageService;

import java.net.http.HttpClient;

@Configuration(proxyBeanMethods = false)
public class XkcdImageClient {

    private final XkcdProperties properties;

    public XkcdImageClient(XkcdProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestClient comicImageClient(RestClient.Builder builder, ClientHttpRequestFactory requestFactory) {
        return builder
                .requestFactory(requestFactory)
                .baseUrl(java.util.Objects.requireNonNull(properties.getImageBaseUrl(), "xkcd.image-base-url must be set").toString())
                .defaultHeader("User-Agent", "XKCD Fetcher - Image Client")
                .build();
    }

    @Bean
    public XKCDImageService imageServiceProxyFactory(@Qualifier("comicImageClient")RestClient comicImageClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(comicImageClient))
                .build()
                .createClient(XKCDImageService.class);
    }
}
