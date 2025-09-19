package se.disabledsecurity.xkcd.fetcher.service;

import se.disabledsecurity.xkcd.fetcher.entity.Comic;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DatabaseService {
    /**
     * Saves the comics to the database and returns the saved entities.
     *
     * @param comics the comics to save (must not be null; elements must not be null)
          * @return the saved entities; never null (may be empty if input had no savable items)
     * @throws IllegalArgumentException if comics is null or contains null elements
     */
    List<Comic> saveComics(Iterable<Comic> comics);

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
    void deleteComicByDate(LocalDate date);

    /**
     * Returns all comics.
     */
    List<Comic> findAllComics();

    /**
     * Gets the highest comic number stored in the database.
     *
     * @return the highest comic number, or empty if no comics exist
     */
    Optional<Integer> getHighestComicNumber();
}
