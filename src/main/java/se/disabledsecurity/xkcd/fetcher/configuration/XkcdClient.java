package se.disabledsecurity.xkcd.fetcher.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;
import se.disabledsecurity.xkcd.fetcher.functions.Functions;
import se.disabledsecurity.xkcd.fetcher.service.XKCDService;

import java.net.URL;

@Configuration(proxyBeanMethods = false)
public class XkcdClient {

    private final XkcdProperties properties;

    public XkcdClient(XkcdProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebClient comicClient(WebClient.Builder builder) {
        return builder
                .baseUrl(properties.getBaseUrl().toString())
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "XKCD Fetcher")
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate") // Support multiple compression formats
                .build();
    }

    @Bean
    public XKCDService httpServiceProxyFactory(WebClient comicClient) {
        return HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(comicClient))
                .build()
                .createClient(XKCDService.class);
    }

}