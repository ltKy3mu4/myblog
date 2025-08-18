package ru.yandex.myblog.model.domain;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    private long id;
    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
