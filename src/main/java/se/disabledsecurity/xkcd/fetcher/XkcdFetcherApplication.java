package se.disabledsecurity.xkcd.fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.blockhound.BlockHound;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;

@SpringBootApplication
@EnableConfigurationProperties(XkcdProperties.class)
public class XkcdFetcherApplication {
    static {
        BlockHound.install();
    }

    public static void main(String[] args) {
        SpringApplication.run(XkcdFetcherApplication.class, args);
    }

}
