package se.disabledsecurity.xkcd.fetcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import javax.annotation.Nullable;

@Getter
@Setter
@Entity
@Table(name = "comic")
public class Comic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Nullable
    private Long id;
    
    @Column(name = "comic_number", nullable = false, unique = true)
    @Nullable
    private Integer comicNumber;
    
    @Column(name = "title", columnDefinition = "TEXT")
    @Nullable
    private String title;
    
    @Column(name = "img", columnDefinition = "TEXT")
    @Nullable
    private String img;
    
    @Column(name = "alt", columnDefinition = "TEXT")
    @Nullable
    private String alt;
    
    @Column(name = "publication_date")
    @Nullable
    private LocalDate publicationDate;
}
