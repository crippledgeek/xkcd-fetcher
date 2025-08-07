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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "img")
    private String img;

    @Column(name = "alt")
    private String alt;

    @Column(name = "date")
    private LocalDate date;

    @OneToOne(mappedBy = "comic")
    private ComicDate comicDate;

}