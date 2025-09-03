package se.disabledsecurity.xkcd.fetcher.service;

import io.vavr.control.Try;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;

public interface ComicService {
    /**
     * Fetches all comics.
     *
     * @return a Flux of all comics
     */

   Try<Iterable<Xkcd>> getAllComics();

    /**
     * Backfills/caches images for comics stored in the database when missing in object storage.
     */
    void backfillImagesFromDb();

    /**
     * Fetches a comic by its ID.
     *
     * @param comicId the ID of the comic to fetch
     * @return the comic with the specified ID
     */
    Xkcd getComicById(int comicId);
}
