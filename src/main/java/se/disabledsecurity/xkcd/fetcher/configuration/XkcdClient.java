package se.disabledsecurity.xkcd.fetcher.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;
import se.disabledsecurity.xkcd.fetcher.service.XKCDComicService;

import java.net.http.HttpClient;

@Configuration(proxyBeanMethods = false)
public class XkcdClient {

    private final XkcdProperties properties;

    public XkcdClient(XkcdProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestClient comicClient(RestClient.Builder builder, ClientHttpRequestFactory requestFactory) {
        return builder
            .requestFactory(requestFactory)
            .baseUrl(properties.getComicBaseUrl().toString())
            .defaultHeader("Accept", "application/json")
            .defaultHeader("User-Agent", "XKCD Fetcher")
            .build();
    }

    @Bean
    public XKCDComicService comicServiceProxyFactory(RestClient comicClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(comicClient))
                .build()
                .createClient(XKCDComicService.class);
    }
}