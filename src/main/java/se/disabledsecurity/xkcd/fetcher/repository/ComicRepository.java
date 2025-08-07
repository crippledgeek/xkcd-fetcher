package se.disabledsecurity.xkcd.fetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Long> {
    List<Comic> findByTitle(String title);
    List<Comic> findByDate(LocalDate date);
}