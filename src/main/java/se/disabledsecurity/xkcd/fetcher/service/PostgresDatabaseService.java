package se.disabledsecurity.xkcd.fetcher.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDate;
import se.disabledsecurity.xkcd.fetcher.repository.ComicDateRepository;
import se.disabledsecurity.xkcd.fetcher.repository.ComicRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
@Slf4j
public class PostgresDatabaseService implements DatabaseService {

    private final ComicRepository comicRepository;
    private final ComicDateRepository comicDateRepository;

    public PostgresDatabaseService(ComicRepository comicRepository, ComicDateRepository comicDateRepository) {
        this.comicRepository = comicRepository;
        this.comicDateRepository = comicDateRepository;
    }

    @Override
    public void saveComics(Iterable<Comic> comics) {
        log.debug("Saving comics to database");
        
        // First, collect all unique dates and ensure they exist in the database
        Set<LocalDate> uniqueDates = StreamSupport.stream(comics.spliterator(), false)
                .map(comic -> comic.getComicDate() != null ? comic.getComicDate().getDate() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // Fetch existing dates from database
        List<ComicDate> existingDates = comicDateRepository.findAllById(uniqueDates);
        Map<LocalDate, ComicDate> dateMap = existingDates.stream()
                .collect(Collectors.toMap(ComicDate::getDate, date -> date));
        
        // Create any missing dates
        for (LocalDate date : uniqueDates) {
            if (!dateMap.containsKey(date)) {
                ComicDate newDate = new ComicDate();
                newDate.setDate(date);
                ComicDate savedDate = comicDateRepository.save(newDate);
                dateMap.put(date, savedDate);
            }
        }
        
        // Process each comic individually for upsert logic
        for (Comic incoming : comics) {
            if (incoming.getComicNumber() == null) {
                // Skip entities without a comic number as we cannot correlate them
                log.warn("Skipping comic without comic number");
                continue;
            }

            // Save or update Comic
            Comic savedComic = comicRepository.findByComicNumber(incoming.getComicNumber())
                    .map(existing -> {
                        log.debug("Updating existing comic with comic number {}", incoming.getComicNumber());
                        existing.setTitle(incoming.getTitle());
                        existing.setImg(incoming.getImg());
                        existing.setAlt(incoming.getAlt());
                        
                        // Handle date change if needed
                        if (incoming.getComicDate() != null) {
                            LocalDate newDate = incoming.getComicDate().getDate();
                            ComicDate existingDate = existing.getComicDate();
                            
                            if (existingDate == null || !existingDate.getDate().equals(newDate)) {
                                // Remove from old date if it existed
                                if (existingDate != null) {
                                    existingDate.removeComic(existing);
                                }
                                
                                // Add to new date
                                ComicDate managedDate = dateMap.get(newDate);
                                managedDate.addComic(existing);
                            }
                        }
                        
                        return comicRepository.save(existing);
                    })
                    .orElseGet(() -> {
                        log.debug("Creating new comic with comic number {}", incoming.getComicNumber());
                        incoming.setId(null); // ensure insert
                        
                        // Set the proper ComicDate reference
                        if (incoming.getComicDate() != null) {
                            LocalDate date = incoming.getComicDate().getDate();
                            ComicDate managedDate = dateMap.get(date);
                            managedDate.addComic(incoming);
                        }
                        
                        return comicRepository.save(incoming);
                    });
        }
        
        // Ensure all changes are flushed to the database
        comicRepository.flush();
        comicDateRepository.flush();
        
        log.debug("Comics saved successfully");
    }

    @Override
    public void deleteAllComics() {
        log.debug("Deleting all comics");
        
        // Since we're using a bidirectional relationship with cascade,
        // we need to carefully manage the deletion to avoid constraint violations
        
        // First, remove all comics from their dates to break the bidirectional relationship
        List<Comic> allComics = comicRepository.findAll();
        for (Comic comic : allComics) {
            if (comic.getComicDate() != null) {
                comic.getComicDate().removeComic(comic);
            }
        }
        
        // Now delete all comics
        comicRepository.deleteAll();
        comicRepository.flush();
        
        // Clean up any empty dates
        List<ComicDate> allDates = comicDateRepository.findAll();
        List<ComicDate> emptyDates = allDates.stream()
                .filter(date -> date.getComics().isEmpty())
                .collect(Collectors.toList());
        
        comicDateRepository.deleteAll(emptyDates);
        comicDateRepository.flush();
        
        log.debug("All comics deleted successfully");
    }

    @Override
    public void deleteComicByTitle(String title) {
        log.debug("Deleting comics with title: {}", title);
        
        List<Comic> comics = comicRepository.findByTitle(title);
        
        // Remove comics from their dates to break the bidirectional relationship
        for (Comic comic : comics) {
            if (comic.getComicDate() != null) {
                comic.getComicDate().removeComic(comic);
            }
        }
        
        // Delete the comics
        comicRepository.deleteAll(comics);
        comicRepository.flush();
        
        // Clean up any empty dates
        cleanupEmptyDates();
        
        log.debug("Deleted {} comics with title: {}", comics.size(), title);
    }

    @Override
    public void deleteComicById(int comicId) {
        log.debug("Deleting comic with ID: {}", comicId);
        
        comicRepository.findByComicNumber(comicId).ifPresent(comic -> {
            if (comic.getComicDate() != null) {
                comic.getComicDate().removeComic(comic);
            }
            
            comicRepository.delete(comic);
            comicRepository.flush();
            
            // Clean up any empty dates
            cleanupEmptyDates();
        });
        
        log.debug("Comic with ID {} deleted (if it existed)", comicId);
    }

    @Override
    public void deleteComicByDate(LocalDate date) {
        log.debug("Deleting comics with date: {}", date);
        
        // Find the ComicDate entity
        comicDateRepository.findById(date).ifPresent(comicDate -> {
            // Get a copy of the comics to avoid ConcurrentModificationException
            Set<Comic> comicsToRemove = new HashSet<>(comicDate.getComics());
            
            // Remove each comic
            for (Comic comic : comicsToRemove) {
                comicDate.removeComic(comic);
                comicRepository.delete(comic);
            }
            
            comicRepository.flush();
            
            // Delete the date if it's now empty
            if (comicDate.getComics().isEmpty()) {
                comicDateRepository.delete(comicDate);
                comicDateRepository.flush();
            }
        });
        
        log.debug("Comics with date {} deleted (if any existed)", date);
    }
    
    /**
     * Helper method to clean up any empty date records
     */
    private void cleanupEmptyDates() {
        List<ComicDate> allDates = comicDateRepository.findAll();
        List<ComicDate> emptyDates = allDates.stream()
                .filter(date -> date.getComics().isEmpty())
                .collect(Collectors.toList());
        
        if (!emptyDates.isEmpty()) {
            comicDateRepository.deleteAll(emptyDates);
            comicDateRepository.flush();
            log.debug("Cleaned up {} empty date records", emptyDates.size());
        }
    }
}