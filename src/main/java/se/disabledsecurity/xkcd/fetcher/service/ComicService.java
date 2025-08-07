package se.disabledsecurity.xkcd.fetcher.service;

import reactor.core.publisher.Mono;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;

import java.util.List;

public interface ComicService {
    /**
     * Fetches all comics.
     *
     * @return a Flux of all comics
     */

    Mono<List<Xkcd>> getAllComics();

    /**
     * Fetches a comic by its ID.
     *
     * @param comicId the ID of the comic to fetch
     * @return the comic with the specified ID
     */
    Mono<Xkcd> getComicById(int comicId);
}
