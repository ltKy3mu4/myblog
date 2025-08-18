package ru.yandex.myblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.myblog.model.domain.ImageBlob;
import ru.yandex.myblog.model.exception.ImageNotFoundException;
import ru.yandex.myblog.repository.JdbcImageRepo;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ImageService {

    private final JdbcImageRepo imageRepo;

    public ImageBlob getForPost(long postId) {
        var img = imageRepo.getByPostId(postId);
        if (img == null){
            log.error("Image for post {} was not found ", postId);
            throw new ImageNotFoundException("Image for post "+postId+" was not found");
        }
        return img;
    }

}
