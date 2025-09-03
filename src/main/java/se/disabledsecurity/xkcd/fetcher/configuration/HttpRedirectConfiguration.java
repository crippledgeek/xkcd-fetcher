package se.disabledsecurity.xkcd.fetcher.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;

@Configuration(proxyBeanMethods = false)
public class HttpRedirectConfiguration {

    @Bean
    public HttpClient httpRedirectClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    @Bean
    public JdkClientHttpRequestFactory requestFactory(HttpClient httpClient) {
        return new JdkClientHttpRequestFactory(httpClient);

    }
}