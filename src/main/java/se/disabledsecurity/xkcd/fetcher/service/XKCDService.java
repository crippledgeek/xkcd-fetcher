package se.disabledsecurity.xkcd.fetcher.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;

/**
 * Service interface for fetching XKCD comics.
 * Provides methods to retrieve the latest comic and a specific comic by its ID.
 */
public interface XKCDService {

    @GetExchange("/info.0.json")
    Mono<Xkcd> getLatestComic();
    @GetExchange("/{comicId}/info.0.json")
    Mono<Xkcd> getComicById(@PathVariable int comicId);

}
