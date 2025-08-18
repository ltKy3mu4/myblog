package ru.yandex.myblog.model.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.model.domain.Tag;
import ru.yandex.myblog.model.dto.FeedPostDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper {

    @Mapping(target = "textPreview", expression = "java(shortText(post.getText()))")
    @Mapping(target = "tags", expression = "java(convertTags(post.getTags()))")
    FeedPostDto toFeedPostDto(Post post);

    List<FeedPostDto> toFeedPostDtoList(List<Post> posts);


    default String shortText(String text) {
        return text.substring(0, Math.min(text.length(), 300));
    }

    default List<String> convertTags(List<Tag> tags){
        if (tags == null ) return new ArrayList<>();
        return tags.stream().map(Tag::getName).collect(Collectors.toList());
    }
}
