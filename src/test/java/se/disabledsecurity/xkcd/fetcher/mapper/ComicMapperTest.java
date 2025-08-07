package se.disabledsecurity.xkcd.fetcher.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.functions.Functions;

import java.net.URL;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ComicMapperTest {

    @Autowired
    private ComicMapper comicMapper;

    @Test
    void toEntity_shouldMapInternalComicToEntity() throws Exception {
        // Given
        LocalDate publicationDate = LocalDate.of(2025, 8, 1);
        URL imageUrl = new URL("https://imgs.xkcd.com/comics/test_comic.png");
        
        se.disabledsecurity.xkcd.fetcher.internal.model.Comic internalComic = 
            new se.disabledsecurity.xkcd.fetcher.internal.model.Comic(
                "Test news",
                "Test safe title",
                "Test transcript",
                "This is a test comic",
                "Test Comic",
                2000,
                publicationDate,
                imageUrl
            );

        // When
        Comic entityComic = comicMapper.toEntity(internalComic);

        // Then
        assertNotNull(entityComic);
        assertEquals("Test Comic", entityComic.getTitle());
        assertEquals("This is a test comic", entityComic.getAlt());
        assertEquals("https://imgs.xkcd.com/comics/test_comic.png", entityComic.getImg());
        assertEquals(publicationDate, entityComic.getDate());
    }

    @Test
    void toDto_shouldMapEntityToInternalComic() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2025, 8, 1);
        
        Comic entityComic = new Comic();
        entityComic.setTitle("Test Comic");
        entityComic.setAlt("This is a test comic");
        entityComic.setImg("https://imgs.xkcd.com/comics/test_comic.png");
        entityComic.setDate(date);

        // When
        se.disabledsecurity.xkcd.fetcher.internal.model.Comic internalComic = comicMapper.toDto(entityComic);

        // Then
        assertNotNull(internalComic);
        assertEquals("Test Comic", internalComic.title());
        assertEquals("This is a test comic", internalComic.alt());
        assertEquals(Functions.toUrl.apply("https://imgs.xkcd.com/comics/test_comic.png").getOrNull(), internalComic.imageUrl());
        assertEquals(date, internalComic.publicationDate());
        
        // Fields that are ignored in the mapping should be null or have default values
        assertNull(internalComic.news());
        assertNull(internalComic.safe_title());
        assertNull(internalComic.transcript());
        assertEquals(0, internalComic.comicNumber());
    }

    @Test
    void partialUpdate_shouldUpdateOnlyNonNullFields() throws Exception {
        // Given
        Comic existingComic = new Comic();
        existingComic.setTitle("Original Title");
        existingComic.setAlt("Original Alt");
        existingComic.setImg("https://example.com/original.png");
        existingComic.setDate(LocalDate.of(2025, 7, 1));
        
        // Create a partial update with only title and alt
        se.disabledsecurity.xkcd.fetcher.internal.model.Comic partialComic = 
            new se.disabledsecurity.xkcd.fetcher.internal.model.Comic(
                null, // news
                null, // safe_title
                null, // transcript
                "Updated Alt", // alt
                "Updated Title", // title
                0, // comicNumber (ignored)
                null, // publicationDate
                null // imageUrl
            );
        
        // When
        Comic updatedComic = comicMapper.partialUpdate(partialComic, existingComic);
        
        // Then
        assertEquals("Updated Title", updatedComic.getTitle());
        assertEquals("Updated Alt", updatedComic.getAlt());
        // These fields should remain unchanged
        assertEquals("https://example.com/original.png", updatedComic.getImg());
        assertEquals(LocalDate.of(2025, 7, 1), updatedComic.getDate());
    }
    
    @Test
    void stringToUrl_shouldConvertStringToUrl() throws Exception {
        // Given
        String urlString = "https://imgs.xkcd.com/comics/test_comic.png";
        
        // When
        URL url = comicMapper.stringToUrl(urlString);
        
        // Then
        assertNotNull(url);
        assertEquals(urlString, url.toString());
    }
    
    @Test
    void urlToString_shouldConvertUrlToString() throws Exception {
        // Given
        URL url = new URL("https://imgs.xkcd.com/comics/test_comic.png");
        
        // When
        String urlString = comicMapper.urlToString(url);
        
        // Then
        assertEquals("https://imgs.xkcd.com/comics/test_comic.png", urlString);
    }
}