package ru.yandex.myblog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.yandex.myblog.model.domain.ImageBlob;
import ru.yandex.myblog.service.ImageService;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/images/{post_id}")
    @ResponseBody
    public ResponseEntity<byte[]> displayImage(@PathVariable(name = "post_id") Long postId) {
        ImageBlob image = imageService.getForPost(postId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image.getData());
    }
}
