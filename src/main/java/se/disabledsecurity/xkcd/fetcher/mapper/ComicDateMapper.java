package se.disabledsecurity.xkcd.fetcher.mapper;

import org.mapstruct.*;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDate;
import se.disabledsecurity.xkcd.fetcher.entity.ComicDateId;

import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ComicDateMapper {
    @Mapping(target = "id.comicId", source = "id")
    @Mapping(target = "id.date", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "comic", source = "comic")
    ComicDate toEntity(Comic comic);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "date", source = "id.date")
    Comic toDto(ComicDate comicDate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ComicDate partialUpdate(Comic comic, @MappingTarget ComicDate comicDate);
    
    // This method is needed for MapStruct to convert Long to ComicDateId
    default ComicDateId map(Long comicId) {
        if (comicId == null) {
            return null;
        }
        ComicDateId id = new ComicDateId();
        id.setComicId(comicId);
        id.setDate(LocalDate.now());
        return id;
    }

}