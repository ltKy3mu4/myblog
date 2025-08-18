package ru.yandex.myblog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import ru.yandex.myblog.model.domain.ImageBlob;
import ru.yandex.myblog.model.exception.ImageNotFoundException;
import ru.yandex.myblog.service.ImageService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {


    private MockMvc mockMvc;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .setViewResolvers((viewName, locale) -> mock(View.class))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void displayImage_ShouldReturnImage() throws Exception {
        long postId = 1L;
        byte[] imageData = new byte[]{0x12, 0x34, 0x56};
        ImageBlob mockImage = new ImageBlob(1L, postId,  MediaType.IMAGE_JPEG_VALUE, imageData);

        when(imageService.getForPost(postId)).thenReturn(mockImage);

        mockMvc.perform(get("/images/{post_id}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageData));

        verify(imageService).getForPost(postId);
    }

    @Test
    void displayImage_ShouldReturnNotFoundWhenImageDoesNotExist() throws Exception {
        Long postId = 999L;
        when(imageService.getForPost(postId))
                .thenThrow(new ImageNotFoundException("Image for post " + postId + " was not found"));

        mockMvc.perform(get("/images/{post_id}", postId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ImageNotFoundException))
                .andExpect(result -> assertEquals("Image for post 999 was not found",
                        result.getResolvedException().getMessage()));

        verify(imageService).getForPost(postId);
    }

}