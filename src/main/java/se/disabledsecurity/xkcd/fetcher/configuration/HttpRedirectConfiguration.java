package se.disabledsecurity.xkcd.fetcher.configuration;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
public class HttpRedirectConfiguration {


    @Bean
    public RequestConfig httpClientRequestConfig() {
        return RequestConfig.custom()
                .setRedirectsEnabled(true)              // allow redirects
                .setCircularRedirectsAllowed(true)      // allow circular redirects
                .setMaxRedirects(20)                    // safe limit
                .build();
    }

    @Bean
    public CloseableHttpClient apacheHttpClient(RequestConfig requestConfig) {
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .disableCookieManagement()
                .disableAuthCaching()
                .build();
    }

    @Bean
    public ClientHttpRequestFactory requestFactory(CloseableHttpClient httpClient) {
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}