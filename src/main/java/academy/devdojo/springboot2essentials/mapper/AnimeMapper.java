package academy.devdojo.springboot2essentials.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import academy.devdojo.springboot2essentials.domain.Anime;
import academy.devdojo.springboot2essentials.request.AnimePostRequestBody;
import academy.devdojo.springboot2essentials.request.AnimePutRequestBody;

@Mapper(componentModel = "spring")
public abstract class AnimeMapper {
    public static final AnimeMapper INSTANCE = Mappers.getMapper(AnimeMapper.class);
    public abstract Anime toAnime(AnimePostRequestBody animePostRequestBody);
    public abstract Anime toAnime(AnimePutRequestBody animePutRequestBody);
}
