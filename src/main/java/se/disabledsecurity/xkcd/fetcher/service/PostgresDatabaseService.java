package se.disabledsecurity.xkcd.fetcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.repository.ComicRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class PostgresDatabaseService implements DatabaseService {

    private final ComicRepository comicRepository;
    private final TransactionTemplate transactionTemplate;

    public PostgresDatabaseService(ComicRepository comicRepository, TransactionTemplate transactionTemplate) {
        this.comicRepository = comicRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public List<Comic> saveComics(Iterable<Comic> comics) {
        if (comics == null) {
            throw new IllegalArgumentException("Comics iterable must not be null");
        }

        return transactionTemplate.execute(status -> {
            log.debug("Saving comics to database");

            List<Comic> validComics = getValidComics(comics);
            if (validComics.isEmpty()) {
                log.debug("No valid comics to save");
                return Collections.emptyList();
            }

            Map<Integer, Comic> existingComics = loadExistingComics(validComics);
            List<Comic> comicsToSave = mergeComics(validComics, existingComics);

            List<Comic> savedComics = comicRepository.saveAll(comicsToSave);

            log.debug("Successfully saved {} comics", savedComics.size());
            return savedComics;
        });
    }

    private List<Comic> getValidComics(Iterable<Comic> comics) {
        return StreamSupport.stream(comics.spliterator(), false)
                .filter(Objects::nonNull)
                .filter(this::hasValidComicNumber)
                .collect(Collectors.toList());
    }

    private boolean hasValidComicNumber(Comic comic) {
        if (comic.getComicNumber() == null) {
            log.warn("Skipping comic without comic number: {}", comic.getTitle());
            return false;
        }
        return true;
    }

    private Map<Integer, Comic> loadExistingComics(List<Comic> validComics) {
        Set<Integer> comicNumbers = validComics.stream()
                .map(Comic::getComicNumber)
                .collect(Collectors.toSet());

        return comicRepository.findByComicNumberIn(comicNumbers)
                .stream()
                .collect(Collectors.toMap(Comic::getComicNumber, comic -> comic));
    }

    private List<Comic> mergeComics(List<Comic> validComics, Map<Integer, Comic> existingComics) {
        return validComics.stream()
                .map(incoming -> mergeComic(incoming, existingComics.get(incoming.getComicNumber())))
                .collect(Collectors.toList());
    }

    private Comic mergeComic(Comic incoming, @javax.annotation.Nullable Comic existing) {
        if (existing == null) {
            log.debug("Creating new comic with number {}", incoming.getComicNumber());
            incoming.setId(null); // Ensure insert
            return incoming;
        } else {
            log.debug("Updating existing comic with number {}", incoming.getComicNumber());
            updateExistingComic(existing, incoming);
            return existing;
        }
    }

    private void updateExistingComic(Comic existing, Comic incoming) {
        existing.setTitle(incoming.getTitle());
        existing.setImg(incoming.getImg());
        existing.setAlt(incoming.getAlt());
        existing.setPublicationDate(incoming.getPublicationDate());
    }

    @Override
    public void deleteAllComics() {
        transactionTemplate.executeWithoutResult(status -> {
            log.debug("Deleting all comics");
            comicRepository.deleteAll();
            log.debug("All comics deleted successfully");
        });
    }

    @Override
    public void deleteComicByTitle(String title) {
        transactionTemplate.executeWithoutResult(status -> {
            log.debug("Deleting comics with title: {}", title);
            List<Comic> comics = comicRepository.findByTitle(title);
            comicRepository.deleteAll(comics);
            log.debug("Deleted {} comics with title: {}", comics.size(), title);
        });
    }

    @Override
    public void deleteComicById(int comicId) {
        transactionTemplate.executeWithoutResult(status -> {
            log.debug("Deleting comic with ID: {}", comicId);
            comicRepository.findByComicNumber(comicId).ifPresent(comic -> {
                comicRepository.delete(comic);
                log.debug("Comic with ID {} deleted", comicId);
            });
        });
    }

    @Override
    public void deleteComicByDate(LocalDate date) {
        transactionTemplate.executeWithoutResult(status -> {
            log.debug("Deleting comics with date: {}", date);
            List<Comic> comics = comicRepository.findByPublicationDate(date);
            comicRepository.deleteAll(comics);
            log.debug("Deleted {} comics with date: {}", comics.size(), date);
        });
    }

    @Override
    public Optional<Integer> getHighestComicNumber() {
        return comicRepository.findHighestComicNumber();
    }

    @Override
    public List<Comic> findAllComics() {
        return comicRepository.findAll();
    }
}