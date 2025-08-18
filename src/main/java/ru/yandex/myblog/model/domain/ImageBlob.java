package ru.yandex.myblog.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageBlob {

    private long id;
    private long post_id;
    private String fileName;
    private byte[] data;

}
