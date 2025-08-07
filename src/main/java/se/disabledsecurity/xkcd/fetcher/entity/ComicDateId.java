package se.disabledsecurity.xkcd.fetcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ComicDateId implements Serializable {
    @Serial
    private static final long serialVersionUID = -4494873853399622600L;
    @Column(name = "comic_id", nullable = false)
    private Long comicId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ComicDateId entity = (ComicDateId) o;
        return Objects.equals(this.date, entity.date) &&
                Objects.equals(this.comicId, entity.comicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, comicId);
    }

}