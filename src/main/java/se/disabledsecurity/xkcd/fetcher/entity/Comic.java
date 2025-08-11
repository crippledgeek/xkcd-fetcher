package se.disabledsecurity.xkcd.fetcher.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "comic")
public class Comic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "comic_number", nullable = false, unique = true)
    private Integer comicNumber;
    
    @Column(name = "title", columnDefinition = "TEXT")
    private String title;
    
    @Column(name = "img", columnDefinition = "TEXT")
    private String img;
    
    @Column(name = "alt", columnDefinition = "TEXT")
    private String alt;
    
    // Many-to-One relationship: Many comics can share the same date
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_date", referencedColumnName = "date")
    private ComicDate comicDate;
 }
