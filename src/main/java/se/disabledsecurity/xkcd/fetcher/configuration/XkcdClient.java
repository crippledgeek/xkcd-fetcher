package se.disabledsecurity.xkcd.fetcher.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;
import se.disabledsecurity.xkcd.fetcher.service.XKCDComicService;

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
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "XKCD Fetcher")
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