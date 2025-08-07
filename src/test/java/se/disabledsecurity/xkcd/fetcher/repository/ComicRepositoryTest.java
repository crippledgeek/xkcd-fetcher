package se.disabledsecurity.xkcd.fetcher.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ComicRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ComicRepository comicRepository;

    @Test
    void findByDate_shouldReturnComicsWithMatchingDate() {
        // Given
        LocalDate date1 = LocalDate.of(2025, 8, 1);
        LocalDate date2 = LocalDate.of(2025, 8, 2);
        
        Comic comic1 = new Comic();
        comic1.setTitle("Test Comic 1");
        comic1.setImg("https://example.com/comic1.png");
        comic1.setAlt("Test Alt 1");
        comic1.setDate(date1);
        
        Comic comic2 = new Comic();
        comic2.setTitle("Test Comic 2");
        comic2.setImg("https://example.com/comic2.png");
        comic2.setAlt("Test Alt 2");
        comic2.setDate(date2);
        
        entityManager.persist(comic1);
        entityManager.persist(comic2);
        entityManager.flush();
        
        // When
        List<Comic> comicsForDate1 = comicRepository.findByDate(date1);
        List<Comic> comicsForDate2 = comicRepository.findByDate(date2);
        
        // Then
        assertEquals(1, comicsForDate1.size());
        assertEquals("Test Comic 1", comicsForDate1.get(0).getTitle());
        assertEquals(date1, comicsForDate1.get(0).getDate());
        
        assertEquals(1, comicsForDate2.size());
        assertEquals("Test Comic 2", comicsForDate2.get(0).getTitle());
        assertEquals(date2, comicsForDate2.get(0).getDate());
    }
    
    @Test
    void findByDate_shouldReturnEmptyListForNonExistentDate() {
        // Given
        LocalDate existingDate = LocalDate.of(2025, 8, 1);
        LocalDate nonExistentDate = LocalDate.of(2025, 8, 3);
        
        Comic comic = new Comic();
        comic.setTitle("Test Comic");
        comic.setImg("https://example.com/comic.png");
        comic.setAlt("Test Alt");
        comic.setDate(existingDate);
        
        entityManager.persist(comic);
        entityManager.flush();
        
        // When
        List<Comic> comics = comicRepository.findByDate(nonExistentDate);
        
        // Then
        assertTrue(comics.isEmpty());
    }
}