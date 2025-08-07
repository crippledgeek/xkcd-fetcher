package se.disabledsecurity.xkcd.fetcher.service;

import reactor.core.publisher.Flux;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;

public interface DatabaseService {
    /**
     * Saves the comics to the database.
     *
     * @param comics the comics to save
     */
    void saveComics(Flux<Comic> comics);

    /**
     * Deletes all comics from the database.
     */
    void deleteAllComics();

    /**
     * Delete comic by title.
     */
    void deleteComicByTitle(String title);
    /**
     * Deletes a comic by its ID.
     *
     * @param comicId the ID of the comic to delete
     */
    void deleteComicById(int comicId);

    /**
     * delete comic by date.
     */
    void deleteComicByDate(java.time.LocalDate date);
}
