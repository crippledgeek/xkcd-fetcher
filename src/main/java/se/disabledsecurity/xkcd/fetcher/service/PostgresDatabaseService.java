package se.disabledsecurity.xkcd.fetcher.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.repository.ComicRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class PostgresDatabaseService implements DatabaseService {

    private final ComicRepository comicRepository;

    public PostgresDatabaseService(ComicRepository comicRepository) {
        this.comicRepository = comicRepository;
    }

    @Override
    public void saveComics(Iterable<Comic> comics) {
        log.debug("Saving comics to database");

        for (Comic incoming : comics) {
            if (incoming.getComicNumber() == null) {
                log.warn("Skipping comic without comic number");
                continue;
            }

            comicRepository.findByComicNumber(incoming.getComicNumber())
                    .map(existing -> {
                        log.debug("Updating existing comic with comic number {}", incoming.getComicNumber());
                        existing.setTitle(incoming.getTitle());
                        existing.setImg(incoming.getImg());
                        existing.setAlt(incoming.getAlt());
                        existing.setPublicationDate(incoming.getPublicationDate());
                        return comicRepository.save(existing);
                    })
                    .orElseGet(() -> {
                        log.debug("Creating new comic with comic number {}", incoming.getComicNumber());
                        incoming.setId(null); // ensure insert
                        return comicRepository.save(incoming);
                    });
        }

        comicRepository.flush();
        log.debug("Comics saved successfully");
    }

    @Override
    public void deleteAllComics() {
        log.debug("Deleting all comics");
        comicRepository.deleteAll();
        comicRepository.flush();
        log.debug("All comics deleted successfully");
    }

    @Override
    public void deleteComicByTitle(String title) {
        log.debug("Deleting comics with title: {}", title);
        List<Comic> comics = comicRepository.findByTitle(title);
        comicRepository.deleteAll(comics);
        comicRepository.flush();
        log.debug("Deleted {} comics with title: {}", comics.size(), title);
    }

    @Override
    public void deleteComicById(int comicId) {
        log.debug("Deleting comic with ID: {}", comicId);
        comicRepository.findByComicNumber(comicId).ifPresent(comic -> {
            comicRepository.delete(comic);
            comicRepository.flush();
        });
        log.debug("Comic with ID {} deleted (if it existed)", comicId);
    }

    @Override
    public void deleteComicByDate(LocalDate date) {
        log.debug("Deleting comics with date: {}", date);
        List<Comic> comics = comicRepository.findByPublicationDate(date);
        comicRepository.deleteAll(comics);
        comicRepository.flush();
        log.debug("Comics with date {} deleted (if any existed)", date);
    }

    @Override
    public Optional<Integer> getHighestComicNumber() {
        return comicRepository.findHighestComicNumber();
    }
}