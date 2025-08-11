package se.disabledsecurity.xkcd.fetcher.service;

import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;

public interface ComicService {
    /**
     * Fetches all comics.
     *
     * @return a Flux of all comics
     */

   Iterable<Xkcd> getAllComics();

    /**
     * Fetches a comic by its ID.
     *
     * @param comicId the ID of the comic to fetch
     * @return the comic with the specified ID
     */
    Xkcd getComicById(int comicId);
}
