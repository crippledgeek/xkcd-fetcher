package se.disabledsecurity.xkcd.fetcher.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDate;
import se.disabledsecurity.xkcd.fetcher.repository.ComicDateRepository;
import se.disabledsecurity.xkcd.fetcher.repository.ComicRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresDatabaseServiceTest {

    @Mock
    private ComicRepository comicRepository;

    @Mock
    private ComicDateRepository comicDateRepository;

    private PostgresDatabaseService databaseService;

    @BeforeEach
    void setUp() {
        databaseService = new PostgresDatabaseService(comicRepository, comicDateRepository);
    }

    @Test
    void saveComics_shouldSaveComicsToRepository() {
        // Given
        Comic comic1 = new Comic();
        comic1.setTitle("Test Comic 1");
        comic1.setImg("https://example.com/comic1.png");
        comic1.setAlt("Test Alt 1");
        comic1.setDate(LocalDate.of(2025, 7, 28));

        Comic comic2 = new Comic();
        comic2.setTitle("Test Comic 2");
        comic2.setImg("https://example.com/comic2.png");
        comic2.setAlt("Test Alt 2");
        comic2.setDate(LocalDate.of(2025, 7, 29));

        Flux<Comic> comics = Flux.just(comic1, comic2);

        // When
        databaseService.saveComics(comics);

        // Then
        // Verify that save was called for each comic
        verify(comicRepository, timeout(1000).times(2)).save(any(Comic.class));
    }

    @Test
    void deleteAllComics_shouldDeleteAllComics() {
        // When
        databaseService.deleteAllComics();

        // Then
        // Verify that deleteAll was called on both repositories
        verify(comicDateRepository, timeout(1000).times(1)).deleteAll();
        verify(comicRepository, timeout(1000).times(1)).deleteAll();
    }

    @Test
    void deleteComicByTitle_shouldDeleteComicsWithGivenTitle() {
        // Given
        String title = "Test Comic";
        
        Comic comic1 = new Comic();
        comic1.setTitle(title);
        
        Comic comic2 = new Comic();
        comic2.setTitle(title);
        
        ComicDate comicDate = new ComicDate();
        comic2.setComicDate(comicDate);

        List<Comic> comics = Arrays.asList(comic1, comic2);
        when(comicRepository.findByTitle(title)).thenReturn(comics);

        // When
        databaseService.deleteComicByTitle(title);

        // Then
        // Verify that findByTitle was called with the correct title
        verify(comicRepository, timeout(1000).times(1)).findByTitle(title);
        // Verify that delete was called for each comic
        verify(comicRepository, timeout(1000).times(2)).delete(any(Comic.class));
        // Verify that delete was called for the comic date
        verify(comicDateRepository, timeout(1000).times(1)).delete(comicDate);
    }

    @Test
    void deleteComicById_shouldDeleteComicWithGivenId() {
        // Given
        int comicId = 1;
        
        Comic comic = new Comic();
        comic.setId(1L);
        
        ComicDate comicDate = new ComicDate();
        comic.setComicDate(comicDate);
        
        when(comicRepository.findById(1L)).thenReturn(Optional.of(comic));

        // When
        databaseService.deleteComicById(comicId);

        // Then
        // Verify that findById was called with the correct id
        verify(comicRepository, timeout(1000).times(1)).findById(1L);
        // Verify that delete was called for the comic
        verify(comicRepository, timeout(1000).times(1)).delete(comic);
        // Verify that delete was called for the comic date
        verify(comicDateRepository, timeout(1000).times(1)).delete(comicDate);
    }

    @Test
    void deleteComicByDate_shouldDeleteComicsWithGivenDate() {
        // Given
        LocalDate date = LocalDate.of(2025, 7, 28);
        
        Comic comic1 = new Comic();
        comic1.setDate(date);
        
        Comic comic2 = new Comic();
        comic2.setDate(date);
        
        ComicDate comicDate = new ComicDate();
        comic2.setComicDate(comicDate);

        List<Comic> comics = Arrays.asList(comic1, comic2);
        when(comicRepository.findByDate(date)).thenReturn(comics);

        // When
        databaseService.deleteComicByDate(date);

        // Then
        // Verify that findByDate was called with the correct date
        verify(comicRepository, timeout(1000).times(1)).findByDate(date);
        // Verify that delete was called for each comic
        verify(comicRepository, timeout(1000).times(2)).delete(any(Comic.class));
        // Verify that delete was called for the comic date
        verify(comicDateRepository, timeout(1000).times(1)).delete(comicDate);
    }
}