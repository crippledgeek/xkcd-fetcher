package se.disabledsecurity.xkcd.fetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface ComicRepository extends JpaRepository<Comic, Long> {
    List<Comic> findByTitle(String title);
    
    // Find comics by date
    List<Comic> findByComicDateDate(LocalDate date);
    
    // Count comics by date (useful for checking if a date can be deleted)
    long countByComicDateDate(LocalDate date);
    
    Optional<Comic> findByComicNumber(Integer comicNumber);
}