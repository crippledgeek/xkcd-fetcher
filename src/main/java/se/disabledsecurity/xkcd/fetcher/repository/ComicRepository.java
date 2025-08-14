package se.disabledsecurity.xkcd.fetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Long> {
    List<Comic> findByTitle(String title);

    // Find comics by publication date
    List<Comic> findByPublicationDate(LocalDate date);

    Optional<Comic> findByComicNumber(Integer comicNumber);

    // Highest saved comic number (used to fetch incrementally)
    @Query("select max(c.comicNumber) from Comic c")
    Optional<Integer> findHighestComicNumber();
}