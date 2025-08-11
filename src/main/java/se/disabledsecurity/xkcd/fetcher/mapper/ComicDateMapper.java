package se.disabledsecurity.xkcd.fetcher.mapper;

import org.mapstruct.*;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDate;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;

import java.time.DateTimeException;
import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ComicDateMapper {
    
    @Mapping(target = "comics", ignore = true)
    ComicDate toComicDate(LocalDate date);
    
    default LocalDate toLocalDate(ComicDate comicDate) {
        return comicDate != null ? comicDate.getDate() : null;
    }
    
    @Named("fromXkcd")
    default ComicDate fromXkcd(Xkcd xkcd) {
        if (xkcd == null || xkcd.year() == null || xkcd.month() == null || xkcd.day() == null) {
            return null;
        }
        try {
            int year = Integer.parseInt(xkcd.year());
            int month = Integer.parseInt(xkcd.month());
            int day = Integer.parseInt(xkcd.day());
            LocalDate date = LocalDate.of(year, month, day);
            ComicDate comicDate = new ComicDate();
            comicDate.setDate(date);
            return comicDate;
        } catch (NumberFormatException | DateTimeException e) {
            return null;
        }
    }
}
