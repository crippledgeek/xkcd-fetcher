package se.disabledsecurity.xkcd.fetcher.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Named;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ComicMapper {
    
    // External XKCD -> Entity
    @Mapping(target = "comicNumber", source = "num")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "img", source = "img")
    @Mapping(target = "alt", source = "alt")
    @Mapping(target = "publicationDate", source = ".", qualifiedByName = "dateFromXkcd")
    Comic fromXkcdToEntity(Xkcd xkcd);

    // Entity -> Internal model
    @Mapping(target = "news", expression = "java(null)")
    @Mapping(target = "safe_title", expression = "java(null)")
    @Mapping(target = "transcript", expression = "java(null)")
    @Mapping(target = "alt", source = "alt")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "comicNumber", source = "comicNumber")
    @Mapping(target = "publicationDate", source = "publicationDate")
    @Mapping(target = "imageUrl", source = "img", qualifiedByName = "stringToUrl")
    se.disabledsecurity.xkcd.fetcher.internal.model.Comic toInternal(Comic entity);

    @Named("stringToUrl")
    default URL stringToUrl(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Named("dateFromXkcd")
    default LocalDate dateFromXkcd(Xkcd xkcd) {
        if (xkcd == null || xkcd.year() == null || xkcd.month() == null || xkcd.day() == null) {
            return null;
        }
        try {
            int year = Integer.parseInt(xkcd.year());
            int month = Integer.parseInt(xkcd.month());
            int day = Integer.parseInt(xkcd.day());
            return LocalDate.of(year, month, day);
        } catch (NumberFormatException | DateTimeException e) {
            return null;
        }
    }
}
