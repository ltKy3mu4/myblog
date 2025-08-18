package ru.yandex.myblog.model.dto;

import lombok.Data;
import ru.yandex.myblog.model.domain.Comment;

import java.util.List;

@Data
public class FeedPostDto {

    private long id;
    private String title;
    private String textPreview;
    private int likesCount;

    private List<Comment> comments;
    private List<String> tags;

}
