package se.disabledsecurity.xkcd.fetcher.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDate;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDateId;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ComicDateMapperTest {

    @Autowired
    private ComicDateMapper comicDateMapper;

    @Test
    void toEntity_shouldMapComicToComicDate() {
        // Given
        Comic comic = new Comic();
        comic.setId(1L);
        comic.setTitle("Test Comic");
        comic.setAlt("This is a test comic");
        comic.setImg("https://imgs.xkcd.com/comics/test_comic.png");
        comic.setDate(LocalDate.of(2025, 8, 1));

        // When
        ComicDate comicDate = comicDateMapper.toEntity(comic);

        // Then
        assertNotNull(comicDate);
        assertNotNull(comicDate.getId());
        assertEquals(1L, comicDate.getId().getComicId());
        assertNotNull(comicDate.getId().getDate());
        assertEquals(comic, comicDate.getComic());
    }

    @Test
    void toDto_shouldMapComicDateToComic() {
        // Given
        Comic originalComic = new Comic();
        originalComic.setId(1L);
        originalComic.setTitle("Test Comic");
        originalComic.setAlt("This is a test comic");
        originalComic.setImg("https://imgs.xkcd.com/comics/test_comic.png");
        originalComic.setDate(LocalDate.of(2025, 8, 1));
        
        ComicDateId id = new ComicDateId();
        id.setComicId(1L);
        id.setDate(LocalDate.now());
        
        ComicDate comicDate = new ComicDate();
        comicDate.setId(id);
        comicDate.setComic(originalComic);

        // When
        Comic mappedComic = comicDateMapper.toDto(comicDate);

        // Then
        assertNotNull(mappedComic);
        assertEquals(1L, mappedComic.getId());
    }

    @Test
    void partialUpdate_shouldUpdateOnlyNonNullFields() {
        // Given
        ComicDateId id = new ComicDateId();
        id.setComicId(1L);
        id.setDate(LocalDate.of(2025, 8, 1));
        
        Comic originalComic = new Comic();
        originalComic.setId(1L);
        originalComic.setTitle("Original Comic");
        
        ComicDate existingComicDate = new ComicDate();
        existingComicDate.setId(id);
        existingComicDate.setComic(originalComic);
        
        Comic updatedComic = new Comic();
        updatedComic.setId(2L);
        updatedComic.setTitle("Updated Comic");

        // When
        ComicDate updatedComicDate = comicDateMapper.partialUpdate(updatedComic, existingComicDate);

        // Then
        assertNotNull(updatedComicDate);
        // The ID should be updated to reflect the new comic ID
        assertEquals(2L, updatedComicDate.getId().getComicId());
        // The date should be the current date as per the mapper implementation
        assertNotNull(updatedComicDate.getId().getDate());
        // The comic reference is not updated by partialUpdate in the current implementation
        assertEquals(originalComic, updatedComicDate.getComic());
    }
    
    @Test
    void map_shouldConvertLongToComicDateId() {
        // Given
        Long comicId = 1L;
        
        // When
        ComicDateId id = comicDateMapper.map(comicId);
        
        // Then
        assertNotNull(id);
        assertEquals(comicId, id.getComicId());
        assertNotNull(id.getDate());
        // The date should be set to the current date
        assertEquals(LocalDate.now().getYear(), id.getDate().getYear());
        assertEquals(LocalDate.now().getMonth(), id.getDate().getMonth());
        assertEquals(LocalDate.now().getDayOfMonth(), id.getDate().getDayOfMonth());
    }
}