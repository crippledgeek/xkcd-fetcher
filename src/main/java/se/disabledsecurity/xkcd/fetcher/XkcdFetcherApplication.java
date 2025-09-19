package se.disabledsecurity.xkcd.fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.disabledsecurity.xkcd.fetcher.common.GarageProperties;
import se.disabledsecurity.xkcd.fetcher.common.HttpClientProperties;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({XkcdProperties.class, GarageProperties.class, HttpClientProperties.class})
public class XkcdFetcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(XkcdFetcherApplication.class, args);
    }
}



