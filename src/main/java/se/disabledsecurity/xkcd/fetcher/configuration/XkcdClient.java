package se.disabledsecurity.xkcd.fetcher.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;
import se.disabledsecurity.xkcd.fetcher.service.XKCDService;

@Configuration(proxyBeanMethods = false)
public class XkcdClient {

    private final XkcdProperties properties;

    public XkcdClient(XkcdProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestClient comicClient(RestClient.Builder builder) {
        return builder
            .baseUrl(properties.getBaseUrl().toString())
            .defaultHeader("Accept", "application/json")
            .defaultHeader("User-Agent", "XKCD Fetcher")
            .build();
    }

    @Bean
    public XKCDService httpServiceProxyFactory(RestClient comicClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(comicClient))
                .build()
                .createClient(XKCDService.class);
    }


}