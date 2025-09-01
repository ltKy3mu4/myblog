package ru.yandex.myblog.model.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
public class Post {

    private long id;
    private String title;
    private String text;
    private int likesCount;

    private ImageBlob image;
    private List<Comment> comments = new ArrayList<>();
    private List<Tag> tags =new ArrayList<>();


    public Post(long id, String title, String text, int likesCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.likesCount = likesCount;
    }

    public Post(long id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", likesCount=" + likesCount +
                '}';
    }

    public String getTagsAsText(){
        return tags.stream().map(e -> e.getName()).collect(Collectors.joining(" "));
    }
}

