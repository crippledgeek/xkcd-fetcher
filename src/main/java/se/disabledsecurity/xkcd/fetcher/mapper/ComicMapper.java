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

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = ComicDateMapper.class)
public interface ComicMapper {
    
    // External XKCD -> Entity
    @Mapping(target = "comicNumber", source = "num")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "img", source = "img")
    @Mapping(target = "alt", source = "alt")
    @Mapping(target = "comicDate", source = ".", qualifiedByName = "fromXkcd")
    Comic fromXkcdToEntity(Xkcd xkcd);

    // Entity -> Internal model
    @Mapping(target = "news", expression = "java(null)")
    @Mapping(target = "safe_title", expression = "java(null)")
    @Mapping(target = "transcript", expression = "java(null)")
    @Mapping(target = "alt", source = "alt")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "comicNumber", source = "comicNumber")
    @Mapping(target = "publicationDate", source = "comicDate.date")
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
}
