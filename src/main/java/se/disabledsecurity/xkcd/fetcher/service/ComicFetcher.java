package se.disabledsecurity.xkcd.fetcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.exception.NoSuchComicFoundException;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;
import se.disabledsecurity.xkcd.fetcher.functions.Functions;
import se.disabledsecurity.xkcd.fetcher.mapper.ComicMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
@Service
public class ComicFetcher implements ComicService {

    private final XKCDService xkcdService;
    private final DatabaseService databaseService;
    private final ComicMapper comicMapper;

    public ComicFetcher(XKCDService xkcdService, DatabaseService databaseService, ComicMapper comicMapper) {
        this.xkcdService = xkcdService;
        this.databaseService = databaseService;
        this.comicMapper = comicMapper;
    }

    @Override
    public Iterable<Xkcd> getAllComics() {
        int latestId = getLatestComicId();
        List<Xkcd> fetchedComics = IntStream.rangeClosed(1, latestId)
                .filter(Functions.notEquals.apply(404))
                .mapToObj(this::getComicById)
                .toList();

        List<Comic> entities = fetchedComics.stream()
                .map(comicMapper::fromXkcdToEntity)
                .toList();

        databaseService.saveComics(entities);
        return fetchedComics;
    }

    @Override
    public Xkcd getComicById(int comicId) {
        return xkcdService.getComicById(comicId);
    }

    private int getLatestComicId() {
        return Optional
                .ofNullable(xkcdService.getLatestComic())
                .map(Xkcd::num)
                .orElseThrow(() -> {
                    log.error("Failed to fetch the latest comic ID from XKCD service.");
                    return new NoSuchComicFoundException("Could not fetch latest comic ID");
                });
    }
}