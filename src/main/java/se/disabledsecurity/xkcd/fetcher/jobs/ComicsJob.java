package se.disabledsecurity.xkcd.fetcher.jobs;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;
import se.disabledsecurity.xkcd.fetcher.service.ComicService;

// Vavr imports for functional style
import io.vavr.collection.Iterator;
import io.vavr.control.Try;

@Service
@DisallowConcurrentExecution
@Slf4j
public class ComicsJob implements Job {

    private final ComicService comicService;

    public ComicsJob(ComicService comicService) {
        this.comicService = comicService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Iterable<Xkcd> allComics = comicService
                .getAllComics()
                .getOrElse(() -> java.util.List.of());

        int count = Iterator.ofAll(allComics).length();
        log.info("Fetched {} comics", count);

        Try.run(comicService::backfillImagesFromDb)
           .onFailure(e -> log.warn("Image backfill job step failed: {}", e.getMessage()));
    }
}
