package se.disabledsecurity.xkcd.fetcher.service;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.exception.NoSuchComicFoundException;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;
import se.disabledsecurity.xkcd.fetcher.functions.Functions;
import se.disabledsecurity.xkcd.fetcher.internal.model.ComicRange;
import se.disabledsecurity.xkcd.fetcher.mapper.ComicMapper;

import java.util.stream.Collectors;

@Slf4j
@Service
public class ComicFetcher implements ComicService {

    private static final int FIRST_COMIC_ID = 1;
    private static final int BATCH_SIZE = 100;

    private final XKCDComicService xkcdComicService;
    private final DatabaseService posgreDatabaseService;
    private final ComicMapper comicMapper;
    private final ImageStorageService garageImageStorageService;
    private final XKCDImageService xkcdImageService;
    private final XkcdProperties xkcdProperties;

    public ComicFetcher(XKCDComicService xkcdComicService,
                        DatabaseService posgreDatabaseService,
                        ComicMapper comicMapper,
                        ImageStorageService garageImageStorageService,
                        XKCDImageService xkcdImageService,
                        XkcdProperties xkcdProperties) {
        this.xkcdComicService = xkcdComicService;
        this.posgreDatabaseService = posgreDatabaseService;
        this.comicMapper = comicMapper;
        this.garageImageStorageService = garageImageStorageService;
        this.xkcdImageService = xkcdImageService;
        this.xkcdProperties = xkcdProperties;
    }

    @Override
    public Try<Iterable<Xkcd>> getAllComics() {
        log.info("Starting to fetch all comics");

        return getLatestComicId()
                .map(latestId -> new ComicRange(determineStartId(), latestId))
                .filter(range -> range.startId() <= range.latestId())
                .peek(range -> log.info("Fetching comics from {} to {}", range.startId(), range.latestId()))
                .map(range -> fetchComicsInRange(range.startId(), range.latestId()))
                .orElse(() -> {
                    log.info("All comics are already up to date");
                    return Try.success(java.util.List.of());
                })
                .peek(comics -> log.info("Successfully fetched and saved {} comics",
                        comics.size()))
                .map(list -> (Iterable<Xkcd>) list)
                .recover(throwable -> {
                    log.error("Failed to fetch all comics", throwable);
                    throw new RuntimeException("Failed to fetch comics", throwable);
                });
    }

    @Override
    public Xkcd getComicById(int comicId) {
        log.debug("Fetching comic with ID: {}", comicId);
        return xkcdComicService.getComicById(comicId);
    }

    @Override
    public void backfillImagesFromDb() {
        Try.run(() -> {
            List<se.disabledsecurity.xkcd.fetcher.entity.Comic> allComics =
                    List.ofAll(posgreDatabaseService.findAllComics());

            log.info("Starting image backfill check for {} comics", allComics.size());

            java.util.List<se.disabledsecurity.xkcd.fetcher.entity.Comic> comicsNeedingBackfill = allComics
                    .filter(comic -> !xkcdProperties.getExcludedComicNumbers().contains(comic.getComicNumber()))
                    .filter(this::needsImageBackfill)
                    .toJavaList();

            log.info("Found {} comics needing image backfill (out of {} total, {} excluded)",
                    comicsNeedingBackfill.size(), allComics.size(),
                    xkcdProperties.getExcludedComicNumbers().size());

            if (comicsNeedingBackfill.isEmpty()) {
                log.info("No comics need image backfill - all images already exist in storage");
                return;
            }

            comicsNeedingBackfill.parallelStream()
                    .forEach(this::backfillSingleImage);

            log.info("Image backfill completed for {} comics", comicsNeedingBackfill.size());
        }).onFailure(e -> log.error("Backfill images from DB failed", e));
    }

    private boolean needsImageBackfill(Comic comic) {
        boolean exists = garageImageStorageService.imageExistsForComic(comic);
        if (!exists) {
            log.info("Comic {} needs backfill - image does not exist in storage", comic.getComicNumber());
        }
        return !exists;
    }

    private java.util.List<Xkcd> fetchComicsInRange(int startId, int endId) {
        return Stream.rangeClosed(startId, endId)
                .filter(id -> {
                    if (xkcdProperties.getExcludedComicNumbers().contains(id)) {
                        log.info("Skipping excluded comic {}", id);
                        return false;
                    }
                    return true;
                })
                .grouped(BATCH_SIZE)
                .map(io.vavr.collection.List::ofAll)
                .flatMap(batch -> Stream.ofAll(processBatch(batch)))
                .toJavaList();
    }

    private java.util.List<Xkcd> processBatch(List<Integer> batch) {
        log.debug("Processing batch of {} comic IDs", batch.size());

        java.util.List<Xkcd> collected = batch
                .toJavaParallelStream()
                .map(this::fetchComicSafely)
                .filter(Try::isSuccess)
                .map(Try::get)
                .collect(Collectors.toList());

        Option.of(collected)
                .filter(comics -> !comics.isEmpty())
                .peek(this::cacheImages)
                .map(this::saveBatchComics)
                .peek(saved -> log.debug("Batch persisted; {} entities saved", saved.size()));

        return collected;
    }

    private Try<Xkcd> fetchComicSafely(int comicId) {
        return Try.of(() -> getComicById(comicId))
                .onFailure(throwable -> log.warn("Failed to fetch comic {}: {}",
                        comicId, throwable.getMessage()));
    }

    private java.util.List<se.disabledsecurity.xkcd.fetcher.entity.Comic> saveBatchComics(
            java.util.List<Xkcd> xkcdComics) {
        return Option.of(xkcdComics)
                .filter(comics -> !comics.isEmpty())
                .map(comics -> Try.of(() -> comicMapper.fromXkcdToEntity(comics))
                        .map(posgreDatabaseService::saveComics)
                        .onSuccess(saved -> log.debug("Successfully saved batch of {} comics", saved.size()))
                        .onFailure(throwable -> log.error("Failed to save batch of {} comics",
                                comics.size(), throwable))
                        .getOrElse(java.util.List::of))
                .getOrElse(() -> {
                    log.debug("No comics to save in this batch");
                    return java.util.List.of();
                });
    }

    private Try<Integer> getLatestComicId() {
        return Try.of(() ->
                Option.of(xkcdComicService.getLatestComic())
                        .map(Xkcd::num)
                        .getOrElseThrow(() -> {
                            log.error("Failed to fetch the latest comic ID from XKCD service");
                            return new NoSuchComicFoundException("Could not fetch latest comic ID");
                        })
        );
    }

    private int determineStartId() {
        return getHighestSavedComicNumber()
                .map(highest -> Math.max(FIRST_COMIC_ID, highest + 1))
                .onFailure(e -> log.warn("Failed to get highest saved comic number, starting from beginning", e))
                .getOrElse(FIRST_COMIC_ID);
    }

    private Try<Integer> getHighestSavedComicNumber() {
        return Try.of(() ->
                Option.of(posgreDatabaseService.getHighestComicNumber())
                        .flatMap(opt -> Option.of(opt.orElse(null)))
                        .map(Functions.toUnboxed::applyAsInt)
                        .getOrElse(0)
        );
    }

    private void cacheImages(java.util.List<Xkcd> comics) {
        List.ofAll(comics)
                .filter(xkcd -> Option.of(xkcd.img()).filter(url -> !url.isBlank()).isDefined())
                .toJavaParallelStream()
                .forEach(this::cacheSingleImage);
    }

    private void cacheSingleImage(Xkcd xkcd) {
        String fileName = extractFileName(xkcd.img());
        if (fileName.isBlank()) return;

        String key = "xkcd/" + xkcd.num();
        log.debug("Caching image for comic {} with key '{}'", xkcd.num(), key);

        Try.of(() -> xkcdImageService.fetchImage(fileName))
                .filter(this::isValidImageData)
                .map(data -> {
                    log.debug("Successfully fetched image for comic {} (size: {} bytes)", xkcd.num(), data.length);
                    garageImageStorageService.save(key, data);
                    return data;
                })
                .onFailure(e -> handleImageCacheFailure(e, xkcd.num(), fileName));
    }

    private void backfillSingleImage(se.disabledsecurity.xkcd.fetcher.entity.Comic entity) {
        log.info("Processing backfill for comic {}", entity.getComicNumber());

        Option.of(entity.getImg())
                .filter(url -> !url.isBlank())
                .map(this::extractFileName)
                .filter(fileName -> !fileName.isBlank())
                .peek(fileName -> {
                    String key = "xkcd/" + entity.getComicNumber();
                    log.info("Attempting to fetch image for comic {} with fileName '{}' and key '{}'",
                            entity.getComicNumber(), fileName, key);

                    Try.of(() -> xkcdImageService.fetchImage(fileName))
                            .filter(this::isValidImageData)
                            .map(data -> {
                                log.info("Successfully fetched and validated image for comic {} (size: {} bytes)",
                                        entity.getComicNumber(), data.length);
                                garageImageStorageService.save(key, data);
                                log.info("Successfully saved image for comic {} to storage", entity.getComicNumber());
                                return data;
                            })
                            .onFailure(e -> {
                                log.error("Failed to process image for comic {}: {}", entity.getComicNumber(), e.getMessage(), e);
                                handleBackfillFailure(e, entity.getComicNumber(), fileName);
                            });
                })
                .onEmpty(() -> log.warn("Comic {} has no valid image URL or filename", entity.getComicNumber()));
    }

    private boolean isValidImageData(byte[] data) {
        if (data == null) return false;

        return Try.of(() -> javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(data)) != null)
                .getOrElse(false);
    }

    private void handleImageCacheFailure(Throwable e, int comicNum, String fileName) {
        log.warn("Failed to cache image for comic {} ({}): {}", comicNum, fileName, e.getMessage());
    }

    private void handleBackfillFailure(Throwable e, int comicNumber, String fileName) {
        log.warn("Backfill: failed to cache image for comicNumber {} ({}): {}",
                comicNumber, fileName, e.getMessage());
    }

    private String extractFileName(String url) {
        return Option.of(url)
                .filter(u -> !u.isBlank())
                .map(u -> {
                    int comicsIdx = u.indexOf("/comics/");
                    if (comicsIdx >= 0) {
                        int start = comicsIdx + "/comics/".length();
                        return start < u.length() ?
                                u.substring(start).replaceFirst("^/+", "") : "";
                    }
                    int idx = u.lastIndexOf('/') + 1;
                    return (idx <= 0 || idx >= u.length()) ? "" : u.substring(idx);
                })
                .getOrElse("");
    }
}