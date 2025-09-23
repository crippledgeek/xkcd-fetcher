package se.disabledsecurity.xkcd.fetcher.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Named;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;
import se.disabledsecurity.xkcd.fetcher.functions.Functions;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ComicMapper {
    
    // External XKCD -> Entity
    @Mapping(target = "comicNumber", source = "num")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "img", source = "img")
    @Mapping(target = "alt", source = "alt")
    @Mapping(target = "publicationDate", source = ".", qualifiedByName = "dateFromXkcd")
    Comic fromXkcdToEntity(Xkcd xkcd);

    // Iterable mapping: External XKCD list -> Entity list
    List<Comic> fromXkcdToEntity(List<Xkcd> xkcds);

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
        return Functions.TO_URL.apply(url)
                .getOrElseThrow(throwable -> new RuntimeException("Invalid URL format: %s".formatted(url), throwable));
    }

    @Named("dateFromXkcd")
    default LocalDate dateFromXkcd(Xkcd xkcd) {
        return Functions.TO_DATE.apply(xkcd.year(), xkcd.month(), xkcd.day());
    }
}
