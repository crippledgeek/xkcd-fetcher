package se.disabledsecurity.xkcd.fetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDate;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDateId;

@Repository
public interface ComicDateRepository extends JpaRepository<ComicDate, ComicDateId> {
}