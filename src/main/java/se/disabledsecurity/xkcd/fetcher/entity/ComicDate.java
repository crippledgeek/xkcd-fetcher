package se.disabledsecurity.xkcd.fetcher.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "comic_date")
public class ComicDate {
    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    // One-to-Many relationship: One date can have many comics
    @OneToMany(mappedBy = "comicDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comic> comics = new HashSet<>();
    
    // Utility method to add a comic to this date
    public void addComic(Comic comic) {
        comics.add(comic);
        comic.setComicDate(this);
    }
    
    // Utility method to remove a comic from this date
    public void removeComic(Comic comic) {
        comics.remove(comic);
        comic.setComicDate(null);
    }
}