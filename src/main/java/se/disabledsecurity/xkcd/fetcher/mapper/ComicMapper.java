package se.disabledsecurity.xkcd.fetcher.mapper;

import org.mapstruct.*;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.functions.Functions;

import java.net.URL;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = ComicDateMapper.class)
public interface ComicMapper {
    
    @Mapping(target = "img", source = "imageUrl", qualifiedByName = "urlToString")
    @Mapping(target = "date", source = "publicationDate")
    Comic toEntity(se.disabledsecurity.xkcd.fetcher.internal.model.Comic comic);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "imageUrl", source = "img", qualifiedByName = "stringToUrl")
    @Mapping(target = "publicationDate", source = "date")
    @Mapping(target = "comicNumber", ignore = true)
    @Mapping(target = "news", ignore = true)
    @Mapping(target = "safe_title", ignore = true)
    @Mapping(target = "transcript", ignore = true)
    se.disabledsecurity.xkcd.fetcher.internal.model.Comic toDto(Comic comic);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Comic partialUpdate(se.disabledsecurity.xkcd.fetcher.internal.model.Comic c, @MappingTarget Comic comic);
    

    @Named("stringToUrl")
    default URL stringToUrl(String img) {
        return Functions.toUrl.apply(img).getOrNull();
    }
    

    @Named("urlToString")
    default String urlToString(URL url) {
        return Functions.toUrlString.apply(url).getOrNull();
    }
}