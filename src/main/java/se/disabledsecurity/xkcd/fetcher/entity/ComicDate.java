package se.disabledsecurity.xkcd.fetcher.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "comic_date")
public class ComicDate {
    @EmbeddedId
    private ComicDateId id;

    @MapsId("comicId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id")
    private Comic comic;

}