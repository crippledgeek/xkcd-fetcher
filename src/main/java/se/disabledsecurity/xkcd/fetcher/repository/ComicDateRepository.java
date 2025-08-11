package se.disabledsecurity.xkcd.fetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComicDateRepository extends JpaRepository<ComicDate, LocalDate> {
}