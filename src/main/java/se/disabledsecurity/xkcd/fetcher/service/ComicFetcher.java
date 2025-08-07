package se.disabledsecurity.xkcd.fetcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;
import se.disabledsecurity.xkcd.fetcher.internal.model.Comics;
import se.disabledsecurity.xkcd.fetcher.mapper.ComicMapper;

import java.net.URL;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static se.disabledsecurity.xkcd.fetcher.functions.Functions.notEquals;
import static se.disabledsecurity.xkcd.fetcher.functions.Functions.toUrl;

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
    public Mono<List<Xkcd>> getAllComics() {
        return xkcdService.getLatestComic()
                .mapNotNull(Xkcd::num)
                .flatMapMany(comicId ->
                        Flux.range(1, comicId)
                                .filter(notEquals.apply(404))
                                .flatMap(id -> xkcdService.getComicById(id)
                                        .onErrorResume(e -> {
                                            log.warn("Error fetching comic #{}: {}", id, e.getMessage());
                                            return Mono.empty();
                                        })
                                        .subscribeOn(Schedulers.boundedElastic()))
                )
                .collectList()
                .doOnNext(xkcds -> {
                    // Convert Xkcd objects to entity Comic objects and save them to the database
                    Flux<Comic> comicFlux = Flux.fromIterable(xkcds)
                            .map(this::convertXkcdToComic);
                    databaseService.saveComics(comicFlux);
                });
    }

    @Override
    public Mono<Xkcd> getComicById(int comicId) {
        return xkcdService.getComicById(comicId)
                .onErrorResume(e -> {
                    log.warn("Error fetching comic #{}: {}", comicId, e.getMessage());
                    return Mono.empty();
                })
                .doOnNext(xkcd -> {
                    // Convert Xkcd to entity Comic and save it to the database
                    Comic comic = convertXkcdToComic(xkcd);
                    databaseService.saveComics(Flux.just(comic));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Converts an Xkcd object to an entity Comic object using ComicMapper.
     *
     * @param xkcd the Xkcd object to convert
     * @return the converted Comic entity
     */
    private Comic convertXkcdToComic(Xkcd xkcd) {
        // First convert Xkcd to internal Comic model
        se.disabledsecurity.xkcd.fetcher.internal.model.Comic internalComic = convertXkcdToInternalComic(xkcd);
        
        // Then use ComicMapper to convert internal Comic model to entity Comic
        return comicMapper.toEntity(internalComic);
    }
    
    /**
     * Converts an Xkcd object to an internal Comic model.
     *
     * @param xkcd the Xkcd object to convert
     * @return the converted internal Comic model
     */
    private se.disabledsecurity.xkcd.fetcher.internal.model.Comic convertXkcdToInternalComic(Xkcd xkcd) {
        // Convert the date strings to a LocalDate
        LocalDate publicationDate;
        try {
            int year = Integer.parseInt(xkcd.year());
            int month = Integer.parseInt(xkcd.month());
            int day = Integer.parseInt(xkcd.day());
            publicationDate = LocalDate.of(year, month, day);
        } catch (NumberFormatException e) {
            log.warn("Error parsing date for comic #{}: {}", xkcd.num(), e.getMessage());
            publicationDate = LocalDate.now();
        }
        
        // Convert img string to URL using Functions.toUrl
        URL imageUrl = toUrl.apply(xkcd.img())
            .getOrElseGet(e -> {
                log.warn("Error parsing image URL for comic #{}: {}", xkcd.num(), e.getMessage());
                return null;
            });
        
        return new se.disabledsecurity.xkcd.fetcher.internal.model.Comic(
            xkcd.news(),
            xkcd.safe_title(),
            xkcd.transcript(),
            xkcd.alt(),
            xkcd.title(),
            xkcd.num(),
            publicationDate,
            imageUrl
        );
    }
}