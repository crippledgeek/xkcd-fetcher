package se.disabledsecurity.xkcd.fetcher.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface XKCDImageService {

    @GetExchange("/comics/{image:.+}")
    byte[] fetchImage(@PathVariable("image") String image);
}
