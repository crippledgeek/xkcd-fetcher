package se.disabledsecurity.xkcd.fetcher.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDate;
import se.disabledsecurity.xkcd.fetcher.repository.ComicDateRepository;
import se.disabledsecurity.xkcd.fetcher.repository.ComicRepository;

import java.time.LocalDate;

@Service
public class PostgresDatabaseService implements DatabaseService {

    private final ComicRepository comicRepository;
    private final ComicDateRepository comicDateRepository;

    public PostgresDatabaseService(ComicRepository comicRepository, ComicDateRepository comicDateRepository) {
        this.comicRepository = comicRepository;
        this.comicDateRepository = comicDateRepository;
    }


    @Override
    public void saveComics(Flux<Comic> comics) {
        comics
            .flatMap(comic -> Flux.defer(() -> {
                // Here you can add any additional processing if needed
                // For example, logging or transforming the comic before saving
                comicRepository.save(comic);
                return Flux.empty();
            }))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

    @Override
    public void deleteAllComics() {
        Flux.defer(() -> {
            comicDateRepository.deleteAll();
            comicRepository.deleteAll();
            return Flux.empty();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
    }

    @Override
    public void deleteComicByTitle(String title) {
        Flux.defer(() -> Flux.fromIterable(comicRepository.findByTitle(title)))
            .flatMap(comic -> Mono.justOrEmpty(comic.getComicDate())
                .doOnNext(comicDateRepository::delete)
                .then(Mono.fromRunnable(() -> comicRepository.delete(comic)))
                .thenMany(Flux.empty()))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

    @Override
    public void deleteComicById(int comicId) {
        Flux.defer(() -> comicRepository.findById((long) comicId)
            .map(Flux::just)
            .orElse(Flux.empty()))
        .flatMap(comic -> Mono.justOrEmpty(comic.getComicDate())
            .doOnNext(comicDateRepository::delete)
            .then(Mono.fromRunnable(() -> comicRepository.delete(comic)))
            .thenMany(Flux.empty()))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();

    }

    @Override
    public void deleteComicByDate(LocalDate date) {
        Flux.defer(() -> Flux.fromIterable(comicRepository.findByDate(date)))
            .flatMap(comic -> Mono.justOrEmpty(comic.getComicDate())
                .doOnNext(comicDateRepository::delete)
                .then(Mono.fromRunnable(() -> comicRepository.delete(comic)))
                .thenMany(Flux.empty()))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }
}
